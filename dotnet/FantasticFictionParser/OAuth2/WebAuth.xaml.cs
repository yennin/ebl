using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web;
using System.Windows;
using System.Windows.Navigation;

namespace FantasticFictionParser.OAuth2
{

    /// <summary>
    /// Interaction logic for WebAuth.xaml
    /// </summary>
    public partial class WebAuth : Window
    {
        public static readonly string _RedirectUrl = "http://localhost/ebl";
        private static readonly string _AuthUrl = "https://www.dropbox.com/1/oauth2/authorize?client_id={0}&response_type=code&state={1}&redirect_uri={2}";

        private string oauth2State;
        private string appSecret;
        private string appKey;

        public AccessToken Token { get; private set; }

        public bool Result { get; private set; }

        public WebAuth(string appKey, string appSecret)
        {
            this.appSecret = appSecret;
            this.appKey = appKey;

            InitializeComponent();

            Authenticate();
        }

        private void Authenticate()
        {
            this.oauth2State = Guid.NewGuid().ToString("N");
            this.Browser.Navigate(string.Format(_AuthUrl, appKey, oauth2State, _RedirectUrl));
        }

        private void BrowserNavigating(object sender, NavigatingCancelEventArgs e)
        {
            Debug.WriteLine(string.Format("Navigated to {0}", e.Uri));
            if (!e.Uri.ToString().StartsWith(_RedirectUrl, StringComparison.OrdinalIgnoreCase))
            {
                // we need to ignore all navigation that isn't to the redirect uri.
                return;
            }

            try
            {
                getAccessTokens(e.Uri);
            }
            catch (ArgumentException)
            {
                // There was an error in the URI passed to ParseTokenFragment
            }
            finally
            {
                e.Cancel = true;
                this.Close();
            }
        }

        private void CancelClick(object sender, RoutedEventArgs e)
        {
            this.Close();
        }

        private async void getAccessTokens(Uri url)
        {
            if (!this.oauth2State.Equals(GetQueryString("state", url)))
            {
                throw new UnauthorizedAccessException("Potential CSRF attack.");
            }

            var code = GetQueryString("code", url);

            var client = new HttpClient()
            {
                BaseAddress = new Uri("https://api.dropbox.com"),
            };

            Task<HttpResponseMessage> response = client.PostAsync("/1/oauth2/token",
                new FormUrlEncodedContent(new List<KeyValuePair<string, string>> {
                    new KeyValuePair<string,string>("code", code),
                    new KeyValuePair<string,string>("client_id", appKey),
                    new KeyValuePair<string,string>("client_secret", appSecret),
                    new KeyValuePair<string,string>("grant_type", "authorization_code"),
                    new KeyValuePair<string,string>("redirect_uri", _RedirectUrl)
                }));
            string result = await response.Result.Content.ReadAsStringAsync();
            using (StringReader sw = new StringReader(result))
            using (JsonReader jw = new JsonTextReader(sw))
            {
                JsonSerializer serializer = new JsonSerializer();
                Token = serializer.Deserialize<AccessToken>(jw);
                if (Token.access_token != null)
                {
                    this.Result = true;
                }
                else
                {
                    Debug.WriteLine(result);
                    this.Result = false;
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
