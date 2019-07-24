#!/bin/sh

bundle exec htmlproofer ./_site \
  --allow-hash-href \
  --check-favicon  \
  --check-html \
  --disable-external
