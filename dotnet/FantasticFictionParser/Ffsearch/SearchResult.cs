using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Ffsearch
{
    class SearchResult
    {
        public string rank { get; set; }
        public string matchExpr { get; set; }
        public Hits hits { get; set; }
        public Info info { get; set; }
    }
}
