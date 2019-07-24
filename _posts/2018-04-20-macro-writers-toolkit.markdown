---
layout: post
title:  "A Typed-Macro Writer’s Toolkit"
date:   2018-04-20 08:00:00
---

<i>
This is a transcript of a PL Wonks lightning talk.
</i>

<hr />

{% for slide in site.data.macro-writers-slides %}

  <img src="{{ site.url }}{{ slide.image }}"
       alt="Slide {{ forloop.index}}"/>
  {{ slide.desc | markdownify }}

{% endfor %}
