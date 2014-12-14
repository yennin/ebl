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
using FantasticFictionParser.Model;

namespace FantasticFictionParser.Storage
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
            ws.Cells["G1"].Value = "Read";
            ws.Cells["H1"].Value = "Favorite";
            ws.Cells["I1"].Value = "Paper";
            ws.Cells["J1"].Value = "eBook";
            ws.Cells["K1"].Value = "Lent To";
            ws.Cells["A1:K1"].Style.Font.Bold = true;

            // dimensions for picture column
            ws.Column(1).Width = 14.30D;
            for (int i = 2; i < 1000; i++)
            {
                ws.Row(i).Height = 109.00D;
            }
        }

        public void AddRow(Book book)
        {

            if (book.image != null) AddPicture(currentRow, 1, book.GetImage());
            AddHyperlink(ws.Cells[currentRow, 2], book.pfn, book.title);
            AddHyperlink(ws.Cells[currentRow, 3], book.authorUrl, book.authorName);
            ws.Cells[currentRow, 4].Value = book.year;
            ws.Cells[currentRow, 4].Style.Numberformat.Format = "0";
            ws.Cells[currentRow, 5].Value = book.seriesName; 
            ws.Cells[currentRow, 6].Value = book.seriesNumber;
            ws.Cells[currentRow, 6].Style.Numberformat.Format = "0";
            ws.Cells[currentRow, 7].Value = book.isRead ? "Yes" : null;
            ws.Cells[currentRow, 8].Value = book.isFavorite ? "Yes" : null;
            ws.Cells[currentRow, 9].Value = book.isBook ? "Yes" : null;
            ws.Cells[currentRow, 10].Value = book.isEBook ? "Yes" : null;
            ws.Cells[currentRow, 11].Value = book.comment;
            currentRow++;
        }

        private void AddHyperlink(ExcelRange cell, Uri hyperlink, string label)
        {
            cell.Hyperlink = hyperlink;
            cell.Value = label;
            cell.StyleName = "HyperLink";
        }

        private void AddPicture(int row, int column, Image image)
        {
            image = ScaleImage(image, 100, 140);
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
            ws.Column(7).AutoFit();
            ws.Column(8).AutoFit();
            ws.Column(9).AutoFit();
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
