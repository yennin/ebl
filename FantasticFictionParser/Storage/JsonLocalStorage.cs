﻿using FantasticFictionParser.Model;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Storage
{
    class JsonLocalStorage : ILocalStorage
    {
        private string localPath = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        private string localAppFolder;
        private string localStorageFilename;
        private readonly ICollection<Book> books;

        public JsonLocalStorage(ICollection<Book> books)
        {
            localAppFolder = localPath + @"\FFLoader";
            localStorageFilename = localPath + @"\FFLoader\books.json";
            this.books = books;
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


    }
}
