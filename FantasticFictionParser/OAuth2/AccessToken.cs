using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.OAuth2
{
    public class AccessToken
    {
        public string access_token { get; set; }
        public string token_type { get; set; }
        public string uid { get; set; }
    }
}
