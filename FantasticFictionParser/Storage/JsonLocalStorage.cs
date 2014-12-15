using FantasticFictionParser.Model;
using FantasticFictionParser.OAuth2;
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
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace FantasticFictionParser.Storage
{

    class JsonLocalStorage : ILocalStorage
    {
        public static readonly String ConsumerKey = "13iykjmga9k4kyf";
        public static readonly String ConsumerSecret = "00imp9517ymqxim";
        public static readonly String RedirectUrl = "http://localhost/ebl";

        private AccessToken DropboxToken;

        private static readonly string localPath = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        private static readonly string localAppFolder = localPath + @"\FFLoader";
        public static readonly string localStorageFilename = localPath + @"\FFLoader\books.json";
        private static readonly string localKeyStorageFilename = localPath + @"\FFLoader\keys.json";
        private readonly ICollection<Book> books;

        public JsonLocalStorage(ICollection<Book> books)
        {
            this.books = books;
            DropboxToken = LoadTokens();
        }

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
                if (book.image == null)
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

        #region Dropbox Storage
        private AccessToken LoadTokens()
        {
            FileInfo file = new FileInfo(localKeyStorageFilename);
            if (!file.Exists)
            {
                return null;
            }
            return JsonConvert.DeserializeObject<AccessToken>(File.ReadAllText(localKeyStorageFilename));
        }

        private void StoreTokens(AccessToken token)
        {
            string path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            if (!Directory.Exists(localAppFolder))
            {
                Directory.CreateDirectory(localAppFolder);
            }
            using (FileStream fs = File.Open(localKeyStorageFilename, FileMode.Create))
            using (StreamWriter sw = new StreamWriter(fs))
            using (JsonWriter jw = new JsonTextWriter(sw))
            {
                JsonSerializer serializer = new JsonSerializer();
                serializer.Serialize(jw, token);
            }
        }

        private string GetDropboxToken()
        {
            if (DropboxToken == null || DropboxToken.access_token == null)
            {
                WebAuth dialog = new WebAuth(ConsumerKey, ConsumerSecret, RedirectUrl);
                bool result = dialog.ShowDialog().GetValueOrDefault();
                if (result)
                {
                    DropboxToken = dialog.GetToken();
                    StoreTokens(DropboxToken);
                }
                else
                {
                    MessageBox.Show("Could not authenticate to Dropbox", "Authentication Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    throw new UnauthorizedAccessException("Dropbox authentication failed.");
                }
            }
            return DropboxToken.access_token;
        }

        public bool RestoreFromDropbox()
        {
            try
            {
                StoreBooks();
                string accessToken = GetDropboxToken();
                RestClient client = new RestClient("https://api-content.dropbox.com");
                RestRequest request = new RestRequest("1/files/auto/{filename}", Method.GET);

                // add auth token
                request.AddParameter(
                    "Authorization",
                    string.Format("Bearer {0}", accessToken), ParameterType.HttpHeader);

                FileInfo file = new FileInfo(JsonLocalStorage.localStorageFilename);
                request.AddUrlSegment("filename", file.Name);

                IRestResponse response = client.Execute(request);
                ICollection<Book> loaded = new HashSet<Book>(JsonConvert.DeserializeObject<ISet<Book>>(response.Content));
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
            catch (UnauthorizedAccessException)
            {
                return false;
            }
        }

        public bool SaveToDropbox()
        {
            try
            {
                StoreBooks();
                string accessToken = GetDropboxToken();
                RestClient client = new RestClient("https://api-content.dropbox.com");
                RestRequest request = new RestRequest("1/files_put/auto/{filename}", Method.PUT);

                // add auth token
                request.AddParameter(
                    "Authorization",
                    string.Format("Bearer {0}", accessToken), ParameterType.HttpHeader);

                FileInfo file = new FileInfo(JsonLocalStorage.localStorageFilename);
                request.AddUrlSegment("filename", file.Name);

                // add files to upload (works with compatible verbs)
                request.AddJsonBody(books);

                IRestResponse<UploadResponse> response2 = client.Execute<UploadResponse>(request);
                var name = response2.Data.revision;
                return true;

            }
            catch (UnauthorizedAccessException)
            {
                return false;
            }

        }
        #endregion
    }
}
