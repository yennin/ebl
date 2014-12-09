﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FantasticFictionParser.Model
{
    public class Book
    {
        public Boolean hasImage { get; set; }
        public Uri imageLoc { get; set; }
        public byte[] image { get; set; }
        public Uri pfn { get; set; }
        public string seriesName { get; set; }
        public string seriesNumber { get; set; }
        public string title { get; set; }
        public Uri authorUrl { get; set; }
        public string authorName { get; set; }
        public int year { get; set; }
        public bool isRead { get; set; }
        public bool isFavorite { get; set; }
        public string lentTo { get; set; }

        // override object.Equals
        public override bool Equals(object obj)
        {
            if (obj == null || GetType() != obj.GetType())
            {
                return false;
            }

            return pfn.AbsolutePath.Equals(((Book)obj).pfn.AbsolutePath);
        }

        // override object.GetHashCode
        public override int GetHashCode()
        {
            return pfn.AbsolutePath.GetHashCode();
        }
    }
}
