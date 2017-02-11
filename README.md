# Facebook Page Scraper

<!---[![DOI](https://zenodo.org/badge/19221/yhegde/fb-page-scraper.svg)](https://zenodo.org/badge/latestdoi/19221/yhegde/fb-page-scraper)--->
[![DOI](https://zenodo.org/badge/45282738.svg)](https://zenodo.org/badge/latestdoi/45282738)

Facebook Page Scraper is a suite of tools for collecting data from public Facebook Pages using Facebook's Graph API. Using this tool you can 
* download data (posts, comments, etc.) in json format
* process json files and insert data into MySQL database
* download images from the public pages

Facebook Page Scraper is especially built for keeping it running and collecting large amount of historical, current and future data (posts, comments etc.) from multiple public facebook pages. Check config.properties.template file for various configuration options for running the tools.

## Credits and Citing
**Cite as**  

Hegde, Y. (2016). facebob-page-scraper (Version 1.33). Syracuse University, School of Information Studies. Retrieved from https://github.com/yhegde/facebook-page-scraper DOI: 10.5281/zenodo.55940

This software is maintained by
* [Yatish Hegde] (https://github.com/yhegde)
    
## Quick start guide
 
* Download `config.properties.template` and `fb-data-collector.jar` from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Rename `config.properties.template` to `config.properties`, open in a text editor and make relevant changes by following inline instructions

* Start downloading data  
    <pre>java -jar fb-data-collector.jar >> data.log 2>&1 &</pre>

Notes:  
    Your config.properties and your jar files should be located in the same directory  
    Data will be downloaded to data download folder baseDir/download

## Insert data into database

* Install MySQL server

* Download `db.schema.sql` and `fb-inserter.jar` from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Create facebook database
     <pre>CREATE DATABASE facebook 
DEFAULT CHARACTER SET utf8 
DEFAULT COLLATE utf8_general_ci;</pre> 

* Fill in database configurations in your `config.properties` file

* Create tables in your `facebook` database
     <pre>mysql -u root -pPassword facebook < db.schema.sql</pre>

* Start inserting data into `facebook` database  
    <pre>java -jar fb-inserter.jar >> insert.log 2>&1 &</pre>

Note: After inserting into database, your data will be moved to data archive folder baseDir/archive 

## Download Images

* Download `config.properties.template` and `fb-image-collector.jar` from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Rename `config.properties.template` to `config.properties`, open in a text editor and make relevant changes by following inline instructions

* Start downloading images  
    <pre>java -jar fb-image-collector.jar >> image.log 2>&1 &</pre>

Note: Images will be downloaded in the images folder baseDir/images

## Running Stats Collector

If you have a requirement to keep collecting stats data (eg. likes count, comments count, shares count, page likes count) at regular intervals of time about past and future posts from public pages, then you should leave this tool running. This tool will not download json, it will directly write the stats to the database. To keep the history of previous stats for posts, you should set *statsHistory=true* in your config.properties file

* Download `config.properties.template` and `fb-stats-collector.jar` from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Rename `config.properties.template` to `config.properties`, open in a text editor and make relevant changes by following inline instructions

* Start downloading images  
    <pre>java -jar fb-stats-collector.jar >> stats.log 2>&1 &</pre>  

## License  
Copyright [2016] [Yatish Hegde]

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Warning: If required by law, you should obtain necessary permissions for downloading data as given in Facebook's terms and conditions, or from concerned authority who manages the Facebook pages, or as per any other applicable law and regulations. This tool does NOT grant you permissions to dowload data from Facebook. You should obtain the permissions yourself.
