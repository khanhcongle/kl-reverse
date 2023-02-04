# Development Journal

## Wish list

- Unit test and Integration Test (ref: https://vertx.io/blog/unit-and-integration-tests/)
- cacheable exclude mode
- delay caching
- SSL

## Bug fixes
2023-02-04
- fix bug missing information when response body is big

## Log

2023-02-04
- visualization resource could be queried by time (from, to) (e.g. GET localhost:8880?from=1675496922458)

2023-01-31
- *valuable*: supports multiple ports by -Dbind=[proxyPort]:[originPort],...
- [visualizer] display meaningful label (time consumption) instead of simple progress

2023-01-29
- *valuable*: configurable ports. As a developer I want to have an easy way to configure proxy port.
- [visualizer] created

2023-01-13
- recognized that there is existing tool that support caching

2022-12-11
- basic reversed proxy