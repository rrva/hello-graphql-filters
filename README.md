# hello-graphql-filters

Generic filters for graphl-java, using a javascript-like expression language

## Build and package

    ./gradlew assemble

This creates `build/distributions/hello-graphql-filters-1.0-SNAPSHOT.tar`

## Start a server
    
    tar xf build/distributions/hello-graphql-filters-1.0-SNAPSHOT.tar
    cd hello-graphql-filters-1.0-SNAPSHOT/bin/
    ./hello-graphql-filters

## Example filtered GraphQL query

```
{
  myContent(filter: "name == 'Series 2' || id == 1") {
    all {
      id
      name
    }
  }
}
```
