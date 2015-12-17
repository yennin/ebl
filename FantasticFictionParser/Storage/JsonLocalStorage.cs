using Dropbox.Api;
using Dropbox.Api.Files;
using FantasticFictionParser.Model;
using FantasticFictionParser.OAuth2;
using FantasticFictionParser.Properties;
using Newtonsoft.Json;
using RestSharp;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace FantasticFictionParser.Storage
{

    class JsonLocalStorage : ILocalStorage
    {
        public static readonly String ConsumerKey = "13iykjmga9k4kyf";
        public static readonly String ConsumerSecret = "00imp9517ymqxim";

        private static readonly string localPath = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        private static readonly string localAppFolder = localPath + @"\FFLoader";
        public static readonly string localStorageFilename = localPath + @"\FFLoader\books.json";
        private static readonly string localKeyStorageFilename = localPath + @"\FFLoader\keys.json";
        private readonly ICollection<Book> books;

        public JsonLocalStorage(ICollection<Book> books)
        {
            this.books = books;
            InitializeCertPinning();
        }

        #region Manage Books
        public void LoadBooks()
        {
            FileInfo file = new FileInfo(localStorageFilename);
            if (!file.Exists)
            {
                return;
            }
            using (FileStream fs = File.Open(localStorageFilename, FileMode.Open))
            using (StreamReader sw = new StreamReader(fs))
            using (JsonReader jw = new JsonTextReader(sw))
            {
                JsonSerializer serializer = new JsonSerializer();
                ICollection<Book> loaded = new HashSet<Book>(serializer.Deserialize<ISet<Book>>(jw));
                books.Clear();

                foreach (Book book in loaded)
                {
                    if (!AddBook(book))
                    {
                        Debug.WriteLine(string.Format("Book {0} already in collection.", book.title));
                    }
                }
            }
        }

        public void StoreBooks()
        {
            string path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            if (!Directory.Exists(localAppFolder))
            {
                Directory.CreateDirectory(localAppFolder);
            }
            using (FileStream fs = File.Open(localStorageFilename, FileMode.Create))
            using (StreamWriter sw = new StreamWriter(fs))
            using (JsonWriter jw = new JsonTextWriter(sw))
            {
                jw.Formatting = Formatting.Indented;

                JsonSerializer serializer = new JsonSerializer();
                serializer.Serialize(jw, books);
            }
        }

        public bool AddBook(Book book)
        {
            if (books.Contains(book))
            {
                return false;
            }
            else
            {
                if (book.hasImage && book.image == null)
                {
                    book.image = GetImage(book.imageLoc);
                }
                books.Add(book);
                return true;
            }
        }

        public void RemoveBook(Book book)
        {
            books.Remove(book);
        }

        public IEnumerable<Book> GetBooks()
        {
            return books;
        }
        #endregion

        #region counts
        public int Count()
        {
            return books.Count;
        }

        public int ReadCount()
        {
            return books.Count(b => b.isRead);
        }

        public int EBookCount()
        {
            return books.Count(b => b.isEBook);
        }
        #endregion

        #region Image handling
        private byte[] GetImage(string url)
        {
            HttpWebRequest httpWebRequest = (HttpWebRequest)HttpWebRequest.Create(url);
            HttpWebResponse httpWebReponse = (HttpWebResponse)httpWebRequest.GetResponse();
            Stream stream = httpWebReponse.GetResponseStream();
            Image image = Image.FromStream(stream);
            stream.Close();
            return ImageToByte(image, ImageFormat.Jpeg);
        }

        private byte[] ImageToByte(Image image, ImageFormat format)
        {
            using (MemoryStream ms = new MemoryStream())
            {
                // Convert Image to byte[]
                image.Save(ms, format);
                byte[] imageBytes = ms.ToArray();
                return imageBytes;
            }
        }
        #endregion

        #region Dropbox Storage
        private string GetDropboxToken()
        {
            string accessToken = Settings.Default.AccessToken;
            if (string.IsNullOrEmpty(accessToken))
            {
                WebAuth dialog = new WebAuth(ConsumerKey, ConsumerSecret);
                dialog.ShowDialog();
                if (dialog.Result)
                {
                    accessToken = dialog.Token.access_token;
                    Settings.Default.AccessToken = accessToken;
                    Settings.Default.Uid = dialog.Token.uid;

                    Settings.Default.Save();
                }
                else
                {
                    MessageBox.Show("Could not authenticate to Dropbox", "Authentication Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    throw new UnauthorizedAccessException("Dropbox authentication failed.");
                }
            }
            return accessToken;
        }

        private DropboxClient GetDropboxClient()
        {
            string accessToken = GetDropboxToken();
            if (string.IsNullOrEmpty(accessToken))
            {
                MessageBox.Show("Could not authenticate to Dropbox", "Authentication Error", MessageBoxButton.OK, MessageBoxImage.Error);
                throw new UnauthorizedAccessException("Dropbox authentication failed.");
            }

            // Specify socket level timeout which decides maximum waiting time when on bytes are
            // received by the socket.
            var httpClient = new HttpClient(new WebRequestHandler { ReadWriteTimeout = 10 * 1000 })
            {
                // Specify request level timeout which decides maximum time taht can be spent on
                // download/upload files.
                Timeout = TimeSpan.FromMinutes(20)
            };

            return new DropboxClient(accessToken, userAgent: "EBL", httpClient: httpClient);
        }

        public async Task<bool> RestoreFromDropbox()
        {
            try
            {
                StoreBooks();
                DropboxClient client = GetDropboxClient();

                FileInfo file = new FileInfo(JsonLocalStorage.localStorageFilename);

                using (var response = await client.Files.DownloadAsync("/"+file.Name))
                {
                    ICollection<Book> loaded = new HashSet<Book>(JsonConvert.DeserializeObject<ISet<Book>>(await response.GetContentAsStringAsync()));
                    books.Clear();
                    foreach (Book book in loaded)
                    {
                        if (!AddBook(book))
                        {
                            Debug.WriteLine(string.Format("Book {0} already in collection.", book.title));
                        }
                    }
                    return true;
                }
            }
            catch (Exception)
            {
                MessageBox.Show("Error uploading to Dropbox", "Dropbox Error", MessageBoxButton.OK, MessageBoxImage.Error);
                return false;
            }
        }

        public async Task<bool> SaveToDropbox()
        {
            try
            {
                StoreBooks();
                DropboxClient client = GetDropboxClient();

                FileInfo file = new FileInfo(JsonLocalStorage.localStorageFilename);

                string jsonbooks = JsonConvert.SerializeObject(books);
                MemoryStream stream = new MemoryStream(Encoding.UTF8.GetBytes(jsonbooks));

                var response = await client.Files.UploadAsync("/"+file.Name, WriteMode.Overwrite.Instance, body: stream);
                var name = response.Rev;
                return true;

            }
            catch (Exception e)
            {
                MessageBox.Show("Error uploading to Dropbox", "Dropbox Error", MessageBoxButton.OK, MessageBoxImage.Error);
                Debug.WriteLine(string.Format("Error in upload: {0}", e.Message));
                return false;
            }
        }

        /// <summary>
        /// Initializes ssl certificate pinning.
        /// </summary>
        private void InitializeCertPinning()
        {
            ServicePointManager.ServerCertificateValidationCallback = (sender, certificate, chain, sslPolicyErrors) =>
            {
                var root = chain.ChainElements[chain.ChainElements.Count - 1];
                var publicKey = root.Certificate.GetPublicKeyString();

                return DropboxCertHelper.IsKnownRootCertPublicKey(publicKey);
            };
        }

        #endregion
    }
}
