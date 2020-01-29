## Media support

### Thumbnail support
The `media` module generates thumbnail images on the fly, caching them in the ephemeral file store for 
subsequent requests. Thumbnail sizes are limited to prevent the system from being overwhelmed but no API rate limiting 
has been applied yet.

### NOTE:
We use `@EnableMongoRepositories` annotation to let spring configure repository beans. In your application you have to
explicitly use the same annotation to specify the repository packages to scan for repository beans.
