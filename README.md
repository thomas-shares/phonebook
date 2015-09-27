# phonebook

Simple phonebook REST app using [Compojure] and [Prismatic Schema]

[Compojure]: https://github.com/weavejester/compojure
[Prismatic Schema]: https://github.com/Prismatic/schema

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

```
lein ring server
```

To view all the entries in the phonebook:
```
curl http://localhost:3000/v1/phonebook
```
To add an entry to the phonebook:
```
curl http://localhost:3000/v1/phonebook -H "Content-Type: application/data" -X POST -d '{:firstname "fred" :surname "Smith" :phonenumber "012345"}'
```
This will return the UUID of this entry.

To update an entry:
```
curl http://localhost:3000/v1/phonebook/38d77ce0-6073-11e5-960a-d35f77d80ceb -H "Content-Type: application/data" -X PUT -d '{:firstname "Fred" :surname "Smith" :phonenumber "012345"}'
```
And to search for an entry:
```
curl http://localhost:3000/v1/phonebook/search?surname=Smith
```

To delete an entry:
```
curl http://localhost:3000/v1/phonebook/38d77ce0-6073-11e5-960a-d35f77d80ceb -X delete 
```

## License

Copyright © 2015 Thomas van der Veen

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

https://www.eclipse.org/legal/epl-v10.html
