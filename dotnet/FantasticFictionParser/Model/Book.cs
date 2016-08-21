using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Model
{
    public class Book
    {
        public string Id { get; set; }
        public bool hasImage { get; set; }
        public string imageLoc { get; set; }
        public byte[] image { get; set; }
        public string pfn { get; set; }
        public string seriesName { get; set; }
        public string seriesNumber { get; set; }
        public string title { get; set; }
        public string authorUrl { get; set; }
        public string authorName { get; set; }
        public int year { get; set; }
        public bool isRead { get; set; }
        public bool isFavorite { get; set; }
        public string comment { get; set; }
        public bool isBook { get; set; }
        public bool isEBook { get; set; }

        public Book()
        {
            isBook = true;
        }

        public Image GetImage()
        {
            if (image == null) return null;
            MemoryStream ms = new MemoryStream(image, 0, image.Length);
            ms.Write(image, 0, image.Length);
            Image result = new Bitmap(ms);
            return result;
        }

        // override object.Equals
        public override bool Equals(object obj)
        {
            if (obj == null || GetType() != obj.GetType())
            {
                return false;
            }

            return pfn.Equals(((Book)obj).pfn);
        }

        // override object.GetHashCode
        public override int GetHashCode()
        {
            return pfn.GetHashCode();
        }
    }
}
