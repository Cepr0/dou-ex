# DOU ex 

_Companies email extractor from [DOU.UA](https://dou.ua)_

Application was useful if you want to send your CV to Ukrainian IT companies.

## Installing

```
git clone https://github.com/Cepr0/dou-ex.git
cd dou-ex
mvn package
```

## Usage

```
java -jar target/dou-ex-1.0.jar
```

Result of extraction is collected in **data/data.csv** in windows-1251 charset.

## Configuration

If you are behind the proxy or need to change delay between loading portion of the data you can change these parameters in config file **config/dou.yml**:

    useProxy: true
    proxyHost: localhost
    proxyPort: 3128
    # Delay between loading next portion of data from the site, ms
    loadingDataDelay:
      - 1000
      - 3000
