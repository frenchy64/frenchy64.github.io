---
layout: post
title:  "Responsive blog layout"
date:   2019-07-23 08:00:00
---

Matthew Butterick&rsquo;s
[_Practical Typography_](https://practicaltypography.com/) is one of my 
favorite books.
It has been my reference point for anything web-related&mdash;including (clearly)
this blog&mdash;so discovering the second edition was a treat.

The book supports a single-column layout, with
consistent [body text](https://practicaltypography.com/body-text.html):

<p>
<video controls muted preload="auto">
  <source src="{{ site.url }}/images/pt-responsive-low.mp4" type="video/mp4">
  <a href="https://practicaltypography.com/how-to-pay-for-this-book.html">Try resizing and scrolling</a>
</video>
</p>

How? Mostly CSS. Here&rsquo;s how I upgraded this blog.

Font sizes in [_viewport width units_](https://css-tricks.com/fun-viewport-units/)
are proportional to the page width.
To avoid infinitely scaling fonts, CSS queries override the font size.

```css
html {font-size: 2.15vw;}
@media all and (min-width:1000px) {html {font-size: 21.8px;}}
@media all and (max-width:520px) {html {font-size: 18px;}}
```

For single-column, elements like `.aside` are moved inside the body.

<div class="aside">
This .aside element is normally right-justified, left of the body.
With single-column, it is left-justified within the body.
</div>

```css
@media all and (max-width:520px) {
  .post .aside {
    left: 0;
    float: inherit;
    position: inherit;
    width: 100%;
    text-align: left;
  }
}
```

Similar CSS is used to move post headers (try resizing this page).
