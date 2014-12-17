using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Forms;

namespace FantasticFictionParser.OAuth2
{

    /// <summary>
    /// Interaction logic for WebAuth.xaml
    /// </summary>
    public partial class WebAuth : Window
    {
        private static readonly string _AuthUrl = "https://www.dropbox.com/1/oauth2/authorize?client_id={0}&response_type=code&state={1}&redirect_uri={2}";
        private string AppKey { get; set; }
        private string AppSecret { get; set; }
        private string RedirectUrl { get; set; }
        private string csrf;

        private WebBrowser authBrowser = new WebBrowser();
        private AccessToken token;

        public WebAuth(string AppKey, string AppSecret, string RedirectUrl)
        {
            this.AppKey = AppKey;
            this.AppSecret = AppSecret;
            this.RedirectUrl = RedirectUrl;

            InitializeComponent();
            authBrowser.DocumentCompleted += authBrowser_DocumentCompleted;
            windowsFormHost.Child = authBrowser;
            Authenticate();
        }

        private void Authenticate()
        {
            csrf = GenerateCsrfToken();
            authBrowser.Navigate(string.Format(_AuthUrl, AppKey, csrf, RedirectUrl));

        }

        void authBrowser_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
                Debug.WriteLine(string.Format("Navigated to {0}", e.Url));
                if (e.Url.AbsoluteUri.StartsWith(RedirectUrl))
                {
                    getAccessTokens(e.Url);
                }
        }

        public AccessToken GetToken()
        {
            return token;
        }

        private static string GenerateCsrfToken()
        {
            var bytes = new byte[21];
            new RNGCryptoServiceProvider().GetBytes(bytes);
            return Convert.ToBase64String(bytes).Replace("+", "-").Replace("/", "_");
        }

        private async void getAccessTokens(Uri url)
        {
            if (!csrf.Equals(GetQueryString("state", url)))
            {
                throw new UnauthorizedAccessException("Potential CSRF attack.");
            }

            var code = GetQueryString("code", url);

            var client = new HttpClient()
            {
                BaseAddress = new Uri("https://api.dropbox.com"),
            };
            //client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Basic", Convert.ToBase64String(Encoding.ASCII.GetBytes(AppKey + ":" + AppSecret)));

            Task<HttpResponseMessage> response = client.PostAsync("/1/oauth2/token",
                new FormUrlEncodedContent(new List<KeyValuePair<string,string>> {
                    new KeyValuePair<string,string>("code", code),
                    new KeyValuePair<string,string>("client_id", AppKey),
                    new KeyValuePair<string,string>("client_secret", AppSecret),
                    new KeyValuePair<string,string>("grant_type", "authorization_code"),
                    new KeyValuePair<string,string>("redirect_uri", RedirectUrl)
                }));
            string result = await response.Result.Content.ReadAsStringAsync();
            using (StringReader sw = new StringReader(result))
            using (JsonReader jw = new JsonTextReader(sw))
            {
                JsonSerializer serializer = new JsonSerializer();
                token = serializer.Deserialize<AccessToken>(jw);
                if (token.access_token != null)
                {
                    this.DialogResult = true;
                }
                else
                {
                    Debug.WriteLine(result);
                    this.DialogResult = false;
                }
            }
        }

        private String GetQueryString(string key, Uri url)
        {
            string pathAndQuery = url.PathAndQuery;
            int iqs = pathAndQuery.IndexOf('?');
            if (iqs >= 0)
            {
                string query = (iqs < pathAndQuery.Length - 1) ? pathAndQuery.Substring(iqs + 1) : String.Empty;
                string[] split = query.Split('&');
                foreach (string val in split)
                {
                    if (val.StartsWith(key))
                    {
                        string[] keyVal = val.Split('=');
                        if (keyVal.Length == 2)
                        {
                            return keyVal[1];
                        }
                    }
                }
            }
            return null;
        }
    }
}
