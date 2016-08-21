using FantasticFictionParser.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Ffsearch
{
    class SearchStatus
    {
        public ICollection<Book> books { set; get; }
        public int totalResults { set; get; }
        public bool hasResults { get { return totalResults > 0; } }
        public int availableResults { get { return hasResults ? books.Count : 0; } }

        public SearchStatus(ICollection<Book> books, int found)
        {
            this.books = books;
            this.totalResults = found;
        }
    }
}
