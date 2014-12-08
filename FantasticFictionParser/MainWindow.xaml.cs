using FantasticFictionParser.Ffsearch;
using FantasticFictionParser.Model;
using FantasticFictionParser.Storage;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Diagnostics;
using System.Media;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Linq;

namespace FantasticFictionParser
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private ISet<Book> removedBooks = new HashSet<Book>();
        private ExcelStorage excel;
        private ILocalStorage storage;

        public MainWindow()
        {
            InitializeComponent();
            excel = new ExcelStorage();
            storage = new JsonLocalStorage((Books)this.Resources["books"]);
            storage.LoadBooks();
            statusBarLeft.Content = string.Format("{0} books in library.", storage.Count());
        }

        #region Common Event Handlers
        private void DataGrid_Hyperlink_Click(object sender, RoutedEventArgs e)
        {
            Hyperlink link = (Hyperlink)e.OriginalSource;
            Process.Start(link.NavigateUri.AbsoluteUri);
        }
        #endregion

        #region Search Tab Event Handlers
        private void SearchBook_Click(object sender, RoutedEventArgs e)
        {
            SearchStatus result = FFSearch.SearchBooks(titleBox.Text);
            if (result.hasResults)
            {
                resultGrid.DataContext = new ObservableCollection<Book>(result.books); ;
                statusBarLeft.Content = string.Format("Showing {0} of {1} books.", result.availableResults, result.totalResults);
            }
            else
            {
                statusBarLeft.Content = "No results.";
                SystemSounds.Exclamation.Play();
            }
        }

        private void resultGrid_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            DataGrid grid = sender as DataGrid;

            Book book = (Book)grid.SelectedItem;
            AddBook(book);
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
            if (storage.AddBook(book))
            {
                statusBarLeft.Content = string.Format("'{0}' added to library.", book.title);
            }
            else
            {
                statusBarLeft.Content = string.Format("'{0}' already in library.", book.title);
            }
        }
        #endregion

        #region Book Tab Event Handlers
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
                    foreach (Book book in storage.GetBooks())
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

        private void restore_Click(object sender, RoutedEventArgs e)
        {
            storage.LoadBooks();
            statusBarLeft.Content = string.Format("Books restored from local storage.");
        }

        private void saveButton_Click(object sender, RoutedEventArgs e)
        {
            storage.StoreBooks();
            statusBarLeft.Content = string.Format("Books saved to local storage.");
        }

        private void booksTab_GotFocus(object sender, RoutedEventArgs e)
        {
            statusBarLeft.Content = string.Format("{0} books in library.", storage.Count());
        }

        #endregion

        #region Window Events
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            storage.StoreBooks();
        }
        #endregion

        #region Collection View Helper
        private ICollectionView GetCollectionView()
        {
            return CollectionViewSource.GetDefaultView(bookGrid.ItemsSource);
        }

        private void RefreshCollectionViewSource()
        {
            GetCollectionView().Refresh();
        }
        #endregion

        #region BookGrid Functionality
        private void bookGrid_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            DataGrid grid = sender as DataGrid;

            Book book = (Book)grid.SelectedItem;
            storage.RemoveBook(book);
            statusBarLeft.Content = string.Format("'{0}' removed to library.", book.title);
        }

        private void CollectionViewSource_Filter(object sender, FilterEventArgs e)
        {
            Book book = e.Item as Book;
            if (book != null)
            // If filter is turned on, filter completed items.
            {
                if (!string.IsNullOrWhiteSpace(titleFilterEntry.Text))
                {
                    if (book.title != null && book.title.IndexOf(titleFilterEntry.Text, StringComparison.OrdinalIgnoreCase) >= 0)
                    {
                        e.Accepted = true;
                    }
                    else
                    {
                        e.Accepted = false;
                    }
                }
                if (!e.Accepted) return;
                if (!string.IsNullOrWhiteSpace(authorFilterEntry.Text))
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
                if (!string.IsNullOrWhiteSpace(seriesFilterEntry.Text))
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

        private void SetRead(object sender, RoutedEventArgs e)
        {
            ICollection<Book> selectedBooks = bookGrid.SelectedItems.Cast<Book>().ToList();
            foreach (Book book in selectedBooks)
            {
                book.isRead = true;
            }
            RefreshCollectionViewSource();
        }

        private void SetUnread(object sender, RoutedEventArgs e)
        {
            ICollection<Book> selectedBooks = bookGrid.SelectedItems.Cast<Book>().ToList();
            foreach (Book book in selectedBooks)
            {
                book.isRead = false;
            }
            RefreshCollectionViewSource();
        }

        private void SetFavorite(object sender, RoutedEventArgs e)
        {
            ICollection<Book> selectedBooks = bookGrid.SelectedItems.Cast<Book>().ToList();
            foreach (Book book in selectedBooks)
            {
                book.isFavorite = true;
            }
            RefreshCollectionViewSource();
        }

        private void RemoveFavorite(object sender, RoutedEventArgs e)
        {
            ICollection<Book> selectedBooks = bookGrid.SelectedItems.Cast<Book>().ToList();
            foreach (Book book in selectedBooks)
            {
                book.isFavorite = false;
            }
            RefreshCollectionViewSource();
        }
        #endregion


    }
}
