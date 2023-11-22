// Copyright (c) 2023  Egon Willighagen <egon.willighagen@gmail.com>
//
// GPL v3

@Grab(group='io.github.egonw.bacting', module='managers-rdf', version='0.4.0')
@Grab(group='io.github.egonw.bacting', module='managers-ui', version='0.4.0')
@Grab(group='io.github.egonw.bacting', module='managers-zenodo', version='0.4.0')
@Grab(group='io.github.egonw.bacting', module='net.bioclipse.managers.jsoup', version='0.4.0')

import groovy.xml.XmlParser

bioclipse = new net.bioclipse.managers.BioclipseManager(".");
zenodo = new net.bioclipse.managers.ZenodoManager(".");
jsoup = new net.bioclipse.managers.JSoupManager(".");

xml = zenodo.getOAIPMHData(args[0])

def oaiDatacite = new XmlParser().parseText(xml)

org = "Undefined"
orgURL = "https://www.nanosafetycluster.eu/"

oaiDatacite.GetRecord.record.header.setSpec.each { it ->
  if (it.text() == "user-nanosolveit") {
    org = "NanoSolveIT"
    orgURL = "http://nanosolveit.eu/"
  }
}
oaiDatacite.GetRecord.record.metadata.oai_datacite.payload.resource.fundingReferences.fundingReference.each { it ->
  if (it.funderIdentifier.text() == "00k4n6c32" && it.awardNumber.text() == "814572") {
    org = "NanoSolveIT"
    orgURL = "http://nanosolveit.eu/"
  }
}



record = oaiDatacite.GetRecord.record.metadata.oai_datacite.payload.resource
doi = record.identifier.text()
title = record.titles.title[0].text()
date = record.dates.date[0].text()
license = record.rightsList.rights[0].@rightsURI
licenseTitle = record.rightsList.rights[0].text()
description = jsoup.removeHTMLTags(record.descriptions.description[0].text())
keywords = ""
if (record.subjects.subject.size() > 0) {
  keywords = record.subjects.subject.iterator().collect{it.text()}.join(", ")
}
url = "https://doi.org/${doi}"

keywordLine = ""
if (keywords.length() > 0) {
  keywordLine = """
    \"keywords\": \"${keywords}\","""
}

print """
<div style="float: right; width: 200px" class='altmetric-embed' data-badge-type='donut' data-condensed='true' data-badge-details='right' data-doi="${doi}"></div>

## ${title}

<script type="application/ld+json">
  {
    "@context": "https://schema.org/",
    "@type": "Dataset",
    "http://purl.org/dc/terms/conformsTo": { "@type": "CreativeWork", "@id": "https://bioschemas.org/profiles/Dataset/1.0-RELEASE" },
    "@id": "https://doi.org/${doi}",
    "identifier": "${doi}",
    "name": "${title}",
    "description": "${description}",
    "license": "${license}",
    "url": "${url}",${keywordLine}
    "creator": [
      {
        "@type": "Organization",
        "name": "${org}"
      }
    ],
    "datePublished": "${date}"
  }
</script>
* Date: ${date}
* License: [${licenseTitle}](${license})
* Project: [${org}](${orgURL})
* URL: [${url}](${url})
* DOI: ${doi}
"""
