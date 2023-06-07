// Copyright (c) 2023  Egon Willighagen <egon.willighagen@gmail.com>
//
// GPL v3

@Grab(group='io.github.egonw.bacting', module='managers-rdf', version='0.3.3-SNAPSHOT')
@Grab(group='io.github.egonw.bacting', module='managers-ui', version='0.3.3-SNAPSHOT')
@Grab(group='io.github.egonw.bacting', module='managers-zenodo', version='0.3.3-SNAPSHOT')

import groovy.xml.XmlParser

bioclipse = new net.bioclipse.managers.BioclipseManager(".");
zenodo = new net.bioclipse.managers.ZenodoManager(".");

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

record = oaiDatacite.GetRecord.record.metadata.oai_datacite.payload.resource
doi = record.identifier.text()
title = record.titles.title[0].text()
date = record.dates.date[0].text()
license = record.rightsList.rights[0].@rightsURI
licenseTitle = record.rightsList.rights[0].text()
description = record.descriptions.description[0].text()
keywords = record.subjects.subject[0].text()
url = record.alternateIdentifiers.find {
  it.alternateIdentifier.'@alternateIdentifierType'.text() == "url"
}.alternateIdentifier.text()

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
    "url": "${url}",
    "keywords": "${keywords}",
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
