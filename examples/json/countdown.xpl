<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                name="main" expand-text="true"
                version="3.0">
<p:input port="source">
  <p:inline content-type="application/json" expand-text="false">
    {
      "new-years-day":  { "title": "New Year’s Day",  "date":      "01-01" },
      "valentines-day": { "title": "Valentine’s Day", "date":      "02-14" },
      "earth-day":      { "title": "Earth Day",       "date": "1970-04-22" },
      "shakespeare":    { "title": "Shakespeare Day", "date":      "04-23" },
      "halloween":      { "title": "Halloween",       "date":      "10-31" },
      "christmas":      { "title": "Christmas",       "date":      "12-25" },
      "boxing-day":     { "title": "Boxing Day",      "date":      "12-26" }
    }
  </p:inline>
</p:input>
<p:output port="result"
          serialization="map { 'method': 'text' }"/>

<p:option name="event" select="'christmas'"/>

<p:variable name="selected-event"
            as="map(*)?"
            select="map:get(., $event)"/>

<p:choose>
  <p:when test="empty($selected-event)">
    <p:identity>
      <p:with-input>
        <p:inline>Unknown event: {$event}</p:inline>
      </p:with-input>
    </p:identity>
  </p:when>
  <p:otherwise>
    <p:variable name="title" select="$selected-event?title"/>
    <p:variable name="this-year" select="year-from-date(current-date())"/>
    <p:variable name="date" as="xs:date"
                select="if ($selected-event?date castable as xs:date)
                        then xs:date($selected-event?date)
                        else xs:date($this-year || '-' || $selected-event?date)"/>
    <p:variable name="date-in-this-year" as="xs:date"
                select="if (year-from-date($date) = $this-year)
                        then $date
                        else xs:date($this-year || substring(string($date), 5))"/>
    <p:variable name="next-date" as="xs:date"
                select="if ($date-in-this-year lt current-date())
                        then $date-in-this-year + xs:yearMonthDuration('P1Y')
                        else $date-in-this-year"/>
    <p:variable name="days"
                select="days-from-duration($next-date - current-date())"/>
    <p:choose>
      <p:when test="$days = 0">
        <p:identity>
          <p:with-input>
            <p:inline>Today is {$title}!</p:inline>
          </p:with-input>
        </p:identity>
      </p:when>
      <p:when test="$days = 1">
        <p:identity>
          <p:with-input>
            <p:inline>Tomorrow is {$title}!</p:inline>
          </p:with-input>
        </p:identity>
      </p:when>
      <p:otherwise>
        <p:choose>
          <p:when test="$date-in-this-year = $date">
            <p:identity>
              <p:with-input>
                <p:inline>It will be {$title} in {$days} days.</p:inline>
              </p:with-input>
            </p:identity>
          </p:when>
          <p:otherwise>
            <p:variable name="ann"
                        select="year-from-date($next-date) - year-from-date($date)"/>
            <p:identity>
              <p:with-input>
                <p:inline>In {$days} days it will be {$ann} years since {$title}.</p:inline>
              </p:with-input>
            </p:identity>
          </p:otherwise>
        </p:choose>
      </p:otherwise>
    </p:choose>
  </p:otherwise>
</p:choose>

</p:declare-step>
