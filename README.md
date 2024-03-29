# Media support for Lhotse
[![Build status](https://badge.buildkite.com/c9836e3fcc16b33997fa98a23bd25a5687b292c15788b3f1dd.svg?branch=main)](https://buildkite.com/everest-engineering/lhotse-media) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=everest-engineering_lhotse-media&metric=alert_status)](https://sonarcloud.io/dashboard?id=everest-engineering_lhotse-media)

This is a supporting repository for [Lhotse](https://github.com/everest-engineering/lhotse), a starter kit for writing event sourced web applications following domain driven design principles.

The `media` module generates thumbnail images on the fly, caching them in the ephemeral file store for
subsequent requests. Thumbnail sizes are limited to prevent the system from being overwhelmed but no API rate limiting
has been applied yet.


## License
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![License: EverestEngineering](https://img.shields.io/badge/Copyright%20%C2%A9-EVERESTENGINEERING-blue)](https://everest.engineering)

>Talk to us `hi@everest.engineering`.
