using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using System.Windows;

namespace FantasticFictionParser
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {

        public App(){

            AppDomain.CurrentDomain.AssemblyResolve += (sender, args) =>
            {

                String resourceName = "FantasticFictionParser." +
                   new AssemblyName(args.Name).Name + ".dll";


                return LoadAssemply(resourceName);

            };
        }

        private static Assembly LoadAssemply(string resourceName)
        {
            using (var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
            {

                Byte[] assemblyData = new Byte[stream.Length];

                stream.Read(assemblyData, 0, assemblyData.Length);

                return Assembly.Load(assemblyData);

            }

        }

    }

}
