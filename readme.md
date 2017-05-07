# DOUex 

_DOU email extractor_

Extracting company emails from [DOU.UA](https://dou.ua)

Application will be useful if you are going to send your CV to Ukrainian IT companies.

## Installing

```
git clone https://github.com/Cepr0/dou-ex.git
cd dou-ex
mvn package
```

## Usage

```
java -jar target/dou-ex-1.1-RELEASE.jar [category] [city]
```

For example: 

Getting all company emails from DOU:
```
java -jar target/dou-ex-1.1-RELEASE.jar
```

Getting company emails with **Java** vacancies:
```
java -jar target/dou-ex-1.1-RELEASE.jar java
```

Getting company emails with **Java** vacancies for **Kiev**:
```
java -jar target/dou-ex-1.1-RELEASE.jar java Киев
```

Result of extraction is stored in **data/dou-emails-[category]-[city]-{current date/time}.csv** in UTF-8 charset.

_Result example:_

    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Киев;hr@ciklum.com
    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Харьков;kharkov@ciklum.com
    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Львов;sean@ciklum.com
    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Днепр;dp_office@ciklum.com
    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Одесса;odessa@ciklum.com
    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Винница;vinnitsa@ciklum.com
    1;Ciklum;https://jobs.dou.ua/companies/ciklum/offices/;Минск (Беларусь);minsk@ciklum.com
    2;EPAM;https://jobs.dou.ua/companies/epam-systems/offices/;Киев;ua_career@epam.com
    2;EPAM;https://jobs.dou.ua/companies/epam-systems/offices/;Харьков;Olga_Panko@epam.com
    2;EPAM;https://jobs.dou.ua/companies/epam-systems/offices/;Львов;ua_career@epam.com
    2;EPAM;https://jobs.dou.ua/companies/epam-systems/offices/;Днепр;Dmytro_Polyakov@epam.com
    2;EPAM;https://jobs.dou.ua/companies/epam-systems/offices/;Винница;iryna_barchuk@epam.com

## Configuration

If you are behind the proxy or need to change delay between the loading a portion 
of the data you can change these parameters in config file **config/dou.yml**:

    useProxy: true
    proxyHost: localhost
    proxyPort: 3128
    # Delay between loading next portion of data from the site (min/max), ms
    loadingDataDelay:
      - 1000
      - 3000
