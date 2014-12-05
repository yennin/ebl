using OfficeOpenXml;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;
using System.Net;
using System.Windows;
using OfficeOpenXml.Drawing;

namespace FantasticFictionParser
{
    class ExcelStorage
    {
        private ExcelPackage pck;
        private ExcelWorksheet ws;
        private int currentRow = 2;

        public ExcelStorage()
        {
            pck = new ExcelPackage(new MemoryStream());
            ws = pck.Workbook.Worksheets.Add("Books");
            var namedStyle = pck.Workbook.Styles.CreateNamedStyle("HyperLink");   //This one is language dependent
            namedStyle.Style.Font.UnderLine = true;
            namedStyle.Style.Font.Color.SetColor(Color.Blue);
            SetHeaders();
        }

        private void SetHeaders()
        {
            ws.Cells["A1"].Value = "Image";
            ws.Cells["B1"].Value = "Title";
            ws.Cells["C1"].Value = "Author";
            ws.Cells["D1"].Value = "Year";
            ws.Cells["E1"].Value = "Series";
            ws.Cells["F1"].Value = "Sequence";
            ws.Cells["A1:F1"].Style.Font.Bold = true;

            // dimensions for picture column
            ws.Column(1).Width = 14.30D;
            for (int i = 2; i < 1000; i++)
            {
                ws.Row(i).Height = 109.00D;
            }

            //ws.Column(2).Width = 60.00D;
            //ws.Column(3).Width = 30.00D;
            //ws.Column(4).Width = 6.00D;
            //ws.Column(5).Width = 30.00D;
            //ws.Column(6).Width = 10.00D;

        }

        public void AddRow(Book book)
        {

            AddPicture(currentRow, 1, book.imageLoc);
            AddHyperlink(ws.Cells[currentRow, 2], book.pfn, book.title);
            AddHyperlink(ws.Cells[currentRow, 3], book.authorUrl, book.authorName);
            ws.Cells[currentRow, 4].Value = book.year;
            ws.Cells[currentRow, 4].Style.Numberformat.Format = "0";
            ws.Cells[currentRow, 5].Value = book.seriesName; 
            ws.Cells[currentRow, 6].Value = book.seriesNumber;
            ws.Cells[currentRow, 6].Style.Numberformat.Format = "0";
            currentRow++;
        }

        private void AddHyperlink(ExcelRange cell, Uri hyperlink, string label)
        {
            cell.Hyperlink = hyperlink;
            cell.Value = label;
            cell.StyleName = "HyperLink";
        }

        private void AddPicture(int row, int column, Uri url)
        {
            HttpWebRequest httpWebRequest = (HttpWebRequest)HttpWebRequest.Create(url);
            HttpWebResponse httpWebReponse = (HttpWebResponse)httpWebRequest.GetResponse();
            Stream stream = httpWebReponse.GetResponseStream();
            Image image = Image.FromStream(stream);
            image = ScaleImage(image, 100, 140);
            stream.Close();
            ExcelPicture pic = ws.Drawings.AddPicture("pic" + (row).ToString(), image);
            pic.SetPosition(row-1, 0, column-1, 0);
        }




        public void Save(string filename)
        {
            FileInfo file = new FileInfo(filename);
            if (file.Exists)
            {
                try
                {
                    file.Delete();

                }
                catch (IOException ex)
                {
                    MessageBox.Show(ex.Message, "Could not delete file. Please check that it is not open.", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
            }
            ws.Column(2).AutoFit();
            ws.Column(3).AutoFit();
            ws.Column(4).AutoFit();
            ws.Column(5).AutoFit();
            ws.Column(6).AutoFit();
            pck.SaveAs(file);
        }


        private Image ScaleImage(Image image, int maxWidth, int maxHeight)
        {
            var ratioX = (double)maxWidth / image.Width;
            var ratioY = (double)maxHeight / image.Height;
            var ratio = Math.Min(ratioX, ratioY);

            var newWidth = (int)(image.Width * ratio);
            var newHeight = (int)(image.Height * ratio);

            var newImage = new Bitmap(newWidth, newHeight);
            Graphics.FromImage(newImage).DrawImage(image, 0, 0, newWidth, newHeight);
            return newImage;
        }
    }
}
