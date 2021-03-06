﻿using FantasticFictionParser.Model;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Ffsearch
{
    class FFSearch
    {

        private static string FF_IMG_BASE_URL = "http://img1.fantasticfiction.co.uk/thumbs/";

        public static SearchStatus SearchBooks(string searchText)
        {
            
            string bookName = WebUtility.UrlEncode( searchText );

            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(Properties.Resources.searchUrl + bookName);
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
                    return new SearchStatus(null, 0);
                }
                data = data.Substring(start, end - start + 1);

                SearchResult result = JsonConvert.DeserializeObject<SearchResult>(data);
                if (result.hits.found > 0)
                {
                    return new SearchStatus(mapBooks(result.hits.hit), result.hits.found);
                }
                else
                {
                    return new SearchStatus(null, 0);
                }
            }
            else
            {
                throw new ApplicationException(string.Format("HTTP error status {0} returned while searching.", response.StatusCode));
            }
        }

        private static List<Book> mapBooks(List<Hit> hits)
        {
            List<Book> foundBooks = hits.Select(item => new Book()
            {
                hasImage = HasImage(item.Imageloc, item.ImageurlAmazon),
                imageLoc = BuildImageLoc(item.Imageloc, item.ImageurlAmazon),
                pfn = "http://www.fantasticfiction.co.uk/" + item.Pfn,
                seriesName = item.SeriesName,
                seriesNumber = item.SeriesNumber,
                title = item.Title,
                year = int.Parse(item.Year),
                authorUrl = "http://www.fantasticfiction.co.uk/" + item.AuthorUrl,
                authorName = item.AuthorName
            }).ToList();
            return foundBooks;
        }

        private static bool HasImage(string imageloc, string imgurlAmazon)
        {
            return imageloc != null || imgurlAmazon != null;
        }

        private static string BuildImageLoc(string imageloc, string imgurlAmazon)
        {
            if (!HasImage(imageloc, imgurlAmazon))
            {
                return null;
            }
            return imageloc != null ? FF_IMG_BASE_URL + imageloc : imgurlAmazon;
        }
    }
}
