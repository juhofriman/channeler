# channeler

Channeler is a developer proxy. It's primarily meant for adding headers to the proxied requests and responses - such as mock authentication headers and
CORS headers.

## Installation

Build jar with leiningen and create sh script for running it.

```
lein uberjar
```

```bash
#! /bin/bash

java -jar /path/to/channeler-0.1.0-SNAPSHOT-standalone.jar $@
```

By default, channeler looks out for `.channeler.edn` file in the current working directory, which defines proxy setup.

## Simple .channeler.edn

```clojure
{:port 5000 ; port to bind to
 :routes 
  {"/" {:host "localhost" ; host of the upstream 
        :port 40900       ; port of the upstream
        :headers {"X-user-id" "jack"}
        :outbound-headers {"Access-Control-Allow-Origin" "*"}}}}
```

When `GET http://localhost:5000/api/get-kittens` is queried, channeler will proxy the request 
to `GET http://localhost:40900/api/get-kittens` with X-user-id header added to the request.
Channeler will also add Access-Control-Allow-Origin: * header to the response sent to the client.

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
