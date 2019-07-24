---
layout: post
title:  "How I create Automatic Annotation Tools (Quals defense)"
date:   2018-04-04 08:00:00
---

<i>
This is a transcript of a practice talk I gave for my Ph.D. qualifying exam.
I've left in a lot of the mistakes to give it some character, and
to learn a bit about how I talk.
It's also available in different formats 
<a href="https://ambrosebs.com/">here</a>.
</i>

<hr />

{% for slide in site.data.quals-slides %}

  <img src="{{ site.url }}{{ slide.image }}"
       alt="Slide {{ forloop.index}}"/>
  {{ slide.desc | markdownify }}

{% endfor %}
