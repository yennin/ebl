using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace FantasticFictionParser
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private ICollection<Book> books;
        private ISet<Book> removedBooks = new HashSet<Book>();
        private ExcelStorage excel;

        public MainWindow()
        {
            InitializeComponent();
            excel = new ExcelStorage();
        }

        private void SearchBook_Click(object sender, RoutedEventArgs e)
        {
            string urlAddress = "http://www.fantasticfiction.co.uk/db-search/v4/books/?start=0&size=50&return-fields=booktype,title,year,pfn,hasimage,authorsinfo,seriesinfo,db,imageloc&q=";
            string bookName = WebUtility.UrlEncode( titleBox.Text );

           

            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(urlAddress+bookName);
            HttpWebResponse response = (HttpWebResponse)request.GetResponse();

            if (response.StatusCode == HttpStatusCode.OK)
            {
                Stream receiveStream = response.GetResponseStream();
                StreamReader readStream = null;

                readStream = new StreamReader(receiveStream);

                string data = readStream.ReadToEnd();
                response.Close();
                readStream.Close();

                int start = data.IndexOf("{");
                int end = data.LastIndexOf("}");
                if (start < 0 || end < 0)
                {
                    statusBarLeft.Content = "No results.";
                    return;
                }
                data = data.Substring(start, end - start + 1);
                


                SearchResult result = JsonConvert.DeserializeObject<SearchResult>(data);
                if (result.hits.found > 0)
                {
                    ObservableCollection<Book> foundBooks = new ObservableCollection<Book>(mapBooks(result.hits.hit));
                    resultGrid.DataContext = foundBooks;
                    statusBarLeft.Content = string.Format("Showing {0} of {1} books.", foundBooks.Count, result.hits.found);
                }
                else
                {
                    statusBarLeft.Content = "No results.";
                }
            }
        }

        private List<Book> mapBooks(List<Hit> hits)
        {
            List<Book> foundBooks = hits.Select(item => new Book()
            {
                hasImage = "y".Equals(item.Hasimage) ? true : false,
                imageLoc = new Uri("http://img1.fantasticfiction.co.uk/thumbs/"+ item.Imageloc),
                pfn = new Uri( "http://www.fantasticfiction.co.uk/"+ item.Pfn),
                seriesName = item.SeriesName,
                seriesNumber = item.SeriesNumber,
                title = item.Title,
                year = int.Parse(item.Year),
                authorUrl = new Uri("http://www.fantasticfiction.co.uk/"+item.AuthorUrl),
                authorName = item.AuthorName
            }).ToList();
            return foundBooks;
        }

        //private void Window_Activated(object sender, EventArgs e)
        //{
        //    titleBox.Focus();
        //}


        private void DataGrid_Hyperlink_Click(object sender, RoutedEventArgs e)
        {
            Hyperlink link = (Hyperlink)e.OriginalSource;
            Process.Start(link.NavigateUri.AbsoluteUri);
        }

        private void saveMyBooks_Click(object sender, RoutedEventArgs e)
        {
            // Configure save file dialog box
            Microsoft.Win32.SaveFileDialog dlg = new Microsoft.Win32.SaveFileDialog();
            dlg.FileName = "Books"; // Default file name
            dlg.DefaultExt = ".xlsx"; // Default file extension
            dlg.Filter = "Excel Document (.xlsx)|*.xlsx"; // Filter files by extension

            // Show save file dialog box
            Nullable<bool> result = dlg.ShowDialog();

            // Process save file dialog box results
            if (result == true)
            {
                Mouse.OverrideCursor = Cursors.Wait;
                try
                {
                    // Save document
                    string filename = dlg.FileName;
                    foreach (Book book in books)
                    {
                        excel.AddRow(book);
                    }
                    excel.Save(filename);
                }
                finally
                {
                    Mouse.OverrideCursor = null;
                }
            }

        }

        private void resultGrid_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            DataGrid grid = sender as DataGrid;

            Book book = (Book)grid.SelectedItem;
            AddBook(book);
        }

        private void LoadMyBooks()
        {
            books = GetBooks();
            books.Clear();

            string path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            FileInfo file = new FileInfo(path + @"\FFLoader\books.json");
            if (!file.Exists)
            {
                return;
            }
            using (FileStream fs = File.Open(path + @"\FFLoader\books.json", FileMode.Open))
            using (StreamReader sw = new StreamReader(fs))
            using (JsonReader jw = new JsonTextReader(sw))
            {
                JsonSerializer serializer = new JsonSerializer();
                ICollection<Book> loaded = new HashSet<Book>(serializer.Deserialize<ISet<Book>>(jw));
                foreach (Book book in loaded)
                {
                    books.Add(book);
                }
            }
            statusBarLeft.Content = string.Format("{0} books in library.", books.Count);
        }

        private void StoreMyBooks()
        {
            string path = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            if (!Directory.Exists(path + @"\FFLoader")){
                Directory.CreateDirectory(path + @"\FFLoader");
            }
            using (FileStream fs = File.Open(path + @"\FFLoader\books.json", FileMode.Create))
            using (StreamWriter sw = new StreamWriter(fs))
            using (JsonWriter jw = new JsonTextWriter(sw))
            {
                jw.Formatting = Formatting.Indented;

                JsonSerializer serializer = new JsonSerializer();
                serializer.Serialize(jw, books);
            }
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            StoreMyBooks();
        }

        private void Window_Initialized(object sender, EventArgs e)
        {
            LoadMyBooks();
        }

        private void bookGrid_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            DataGrid grid = sender as DataGrid;

            Book book = (Book)grid.SelectedItem;
            books.Remove(book);
            statusBarLeft.Content = string.Format("'{0}' removed to library.", book.title);
        }

        private void restore_Click(object sender, RoutedEventArgs e)
        {
            LoadMyBooks();
            statusBarLeft.Content = string.Format("Books restored from local storage.");
        }

        private void saveButton_Click(object sender, RoutedEventArgs e)
        {
            StoreMyBooks();
            statusBarLeft.Content = string.Format("Books saved to local storage.");
        }

        private void resultGrid_PreviewKeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
            {
                e.Handled = true;
                DataGrid grid = sender as DataGrid;

                Book book = (Book)grid.SelectedItem;
                AddBook(book);
            }
            if (e.Key == Key.Tab)
            {
                titleBox.Focus();
                titleBox.SelectAll();
                UpdateLayout();
            }
        }

        private void AddBook(Book book)
        {
            if (books.Contains(book))
            {
                statusBarLeft.Content = string.Format("'{0}' already in library.", book.title);
            }
            else
            {
                books.Add(book);
                statusBarLeft.Content = string.Format("'{0}' added to library.", book.title);
            }
        }

        private void booksTab_GotFocus(object sender, RoutedEventArgs e)
        {
            statusBarLeft.Content = string.Format("{0} books in library.", books.Count);
        }

        private void titleBox_GotKeyboardFocus(object sender, KeyboardFocusChangedEventArgs e)
        {
            titleBox.SelectAll();
        }

        private void titleBox_GotMouseCapture(object sender, MouseEventArgs e)
        {
            titleBox.SelectAll();
        }

        private void RefreshCollectionViewSource()
        {
            ICollectionView view = CollectionViewSource.GetDefaultView(bookGrid.ItemsSource);
            view.Refresh();
        }

        //private void TabControl_SelectionChanged(object sender, SelectionChangedEventArgs e)
        //{
        //    if (e.Source is TabControl)
        //    {
        //        if (searchTab.IsSelected)
        //        {
                    
        //            titleBox.Focus();
        //            titleBox.SelectAll();
        //            UpdateLayout();
        //            Debug.WriteLine("set focus");
        //        }
        //    }
        //}

        private Books GetBooks()  {
            return (Books)this.Resources["books"]; ;
        }

        private void CollectionViewSource_Filter(object sender, FilterEventArgs e)
        {
            Book book = e.Item as Book;
            if (book != null)
            // If filter is turned on, filter completed items.
            {
                if (titleFilterEntry.Text != null)
                {
                    if (book.title != null && book.title.IndexOf(titleFilterEntry.Text, StringComparison.OrdinalIgnoreCase) >= 0)
                    {
                        e.Accepted = true;
                    }
                    else {
                        e.Accepted = false;
                    }
                }
                if (!e.Accepted) return; 
                if (authorFilterEntry.Text != null)
                {
                    if (book.authorName != null && book.authorName.IndexOf(authorFilterEntry.Text, StringComparison.OrdinalIgnoreCase) >= 0)
                    {
                        e.Accepted = true;
                    }
                    else
                    {
                        e.Accepted = false;
                    }
                }
                if (!e.Accepted) return;
                if (seriesFilterEntry.Text != null)
                {
                    if (book.seriesName != null && book.seriesName.IndexOf(seriesFilterEntry.Text, StringComparison.OrdinalIgnoreCase) >= 0)
                    {
                        e.Accepted = true;
                    }
                    else
                    {
                        e.Accepted = false;
                    }
                }
            }
        }

        private void filterEntry_TextChanged(object sender, TextChangedEventArgs e)
        {
            RefreshCollectionViewSource();
        }

    }
}
