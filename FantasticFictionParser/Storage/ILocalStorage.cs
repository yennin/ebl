using FantasticFictionParser.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Storage
{
    interface ILocalStorage
    {
        void LoadBooks();
        void StoreBooks();
        bool AddBook(Book book);
        void RemoveBook(Book book);
        IEnumerable<Book> GetBooks();
        int Count();
        int ReadCount();
        int EBookCount();

        Task<bool> RestoreFromDropbox();
        Task<bool> SaveToDropbox();
    }
}
