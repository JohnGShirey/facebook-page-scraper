# Facebook Page Scraper

[![DOI](https://zenodo.org/badge/19221/yhegde/fb-page-scraper.svg)](https://zenodo.org/badge/latestdoi/19221/yhegde/fb-page-scraper)

Facebook Page Scraper is a tool for collecting data from public facebook pages. This tool uses Facebook's Graph API to collect data. Using this tool you can download and archive the data in json files, insert them into MySQL database, or download images from public pages.

This tool is especially built for keeping it running and collecting large amount of historical, current and future data (posts, comments etc.) from multiple public facebook pages. Check config.properties.template file for various configuration options for running the tool. 

<pre>Warning: If required by law, you should obtain necessary permissions for downloading data as given in Facebook's terms and conditions, or from concerned authority who manages the Facebook pages, or as per any other applicable law and regulations. This tool does NOT grant you permissions to dowload data from Facebook. You should obtain the permissions yourself.</pre>

## Quick start guide
* Install MySQL server
 
* Download **config.properties.template** and **fb-data-collector.jar** from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Rename config.properties.template to **config.properties**, open in a text editor and make relevant changes by following inline instructions

* Start downloading data  
    <pre>java -jar fb-data-collector.jar >> data.log 2>&1 &</pre>

Notes:  
    Your config.properties and your *.jar files should be located in the same directory  
    Data will be downloaded to data download folder "baseDir/download/"

## Insert data into database

* Download **db.schema.sql** and **fb-inserter.jar** from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Create facebook database
     <pre>CREATE DATABASE facebook 
DEFAULT CHARACTER SET utf8 
DEFAULT COLLATE utf8_general_ci;</pre> 

* Fill database configurations in your config.properties file

* Create tables in your `facebook` database
     <pre>mysql -u root -pPassword facebook < db.schema.sql</pre>

* Start inserting data into `facebook` database  
    <pre>java -jar fb-inserter.jar >> insert.log 2>&1 &</pre>

Note: After insert into database, your data will be moved to data archive folder "baseDir/archive/" 

## Download Images

* Download **config.properties.template** and **fb-image-collector.jar** from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/)

* Rename config.properties.template to **config.properties**, open in a text editor and make relevant changes by following inline instructions

* Start downloading images  
    <pre>java -jar fb-image-collector.jar >> image.log 2>&1 &</pre>

## Credits and Citing

This software is maintained by  
* [Yatish Hegde] (https://github.com/yhegde)  

Cite as  

<pre>fb-page-scraper (2016). Version 1.2. DOI: 10.5281/zenodo.50451</pre>


## License  
Copyright [2016] [Yatish Hegde]

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
