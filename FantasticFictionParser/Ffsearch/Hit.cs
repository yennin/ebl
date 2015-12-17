using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Ffsearch
{
    public class Hit
    {
        public string id { get; set; }
        public Data data { get; set; }

        public string AuthorUrl
        {
            get { return SplitAuthorInfo(0); }
        }

        public string AuthorName
        {
            get { return System.Net.WebUtility.HtmlDecode(SplitAuthorInfo(1)); }
        }

        public string Hasimage
        {
            get { return data.hasimage.FirstOrDefault(); }
        }

        public string Imageloc
        {
            get { return data.imageloc.FirstOrDefault(); }
        }

        public string ImageurlAmazon
        {
            get { return data.imageurl_amazon.FirstOrDefault(); }
        }

        public string Pfn
        {
            get { return data.pfn.FirstOrDefault(); }
        }

        public string SeriesName
        {
            get { return SplitSeriesInfo(0); }
        }

        public string SeriesNumber
        {
            get { return SplitSeriesInfo(1); }
        }

        public string Title
        {
            get { return System.Net.WebUtility.HtmlDecode(data.title.FirstOrDefault()); }
        }

        public string Year
        {
            get { return data.year.FirstOrDefault(); }
        }

        private string SplitAuthorInfo(int pos)
        {
            return data.authorsinfo.FirstOrDefault().Split('|').ElementAt(pos);
        }

        private string SplitSeriesInfo(int pos)
        {
            if (data.seriesinfo.FirstOrDefault() == null) return null;
            return data.seriesinfo.FirstOrDefault().Split('|').ElementAt(pos);
        }
    }
}
