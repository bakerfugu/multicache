# multicache
For easy and modular customization of multiple local and remote caching with redis and caffeine for SpringBoot

## How do I use it?

You'll need to download the custom starter because I have not uploaded this project to the Maven Central Repository. Add the starter to your project, then add `multicache-spring-boot-starter` as a dependency in your pom.xml:
```
<dependency>
    <artifactId>multicache-spring-boot-starter</artifactId>
    <groupId>com.tvg.cache</groupId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Make sure your have redis and caffeine aswell.

That's most of the hard work. From this point, you can now use your application.properties file to have unlimited redis and caffeine caches with unique configurations, for example:
```
tvg.multicache.enable-caffeine=true
tvg.multicache.enable-redis=true
tvg.multicache.redis.contacts.time-to-live=2h
tvg.multicache.redis.contacts.use-key-prefix=false
tvg.multicache.redis.contacts.key-prefix=my-custom-contacts
tvg.multicache.redis.my-other-redis-cache.key-prefix=some_prefix
tvg.multicache.caffeine.local-contacts.spec=recordStats
tvg.multicache.caffeine.other-local-contacts.spec=expireAfterAccess=50s,recordStats
```

The above configuration sets up 4 caches. Redis caches are `contacts` and `my-other-redis-cache`, and Caffeine caches are `local-contacts` and `other-local-contacts`.

Now, anywhere where you would use the Spring `@Cacheable` annotation you can use `@Cacheable("CACHE_NAME")`. For example: 
```
@Cacheable("contacts")
public Contact findById(@PathVariable long id) {

    if (repository.existsById(id)) {
        log.info(String.format("Contact (%d) not found in cache, retrieving now", id));
        return repository.findById(id).get();
    }

    throw new ContactNotFoundException(id);
}
```
