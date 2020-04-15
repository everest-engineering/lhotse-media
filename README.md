# Media support for Lhotse
[![Build Status](https://travis-ci.com/everest-engineering/lhotse-media.svg?branch=master)](https://travis-ci.com/everest-engineering/lhotse-media)

This is a supporting repository for [Lhotse](https://github.com/everest-engineering/lhotse), a starter kit for writing event sourced web applications following domain driven design principles.

The `media` module generates thumbnail images on the fly, caching them in the ephemeral file store for 
subsequent requests. Thumbnail sizes are limited to prevent the system from being overwhelmed but no API rate limiting 
has been applied yet.
