# digital-content

## Overview

An extremely vaguely named application that serves to expose files over HTTP.  This application facilitates access to
files at a persistent URL.  While the current implementation serves files from a local/mounted filesystem, it could
be updated to expose content stored in one or more repository systems.

## Features

* Files have identifiers and can be retrieved by simple URLs of the format: http://host/id
* From the simple URLs clients will be redirected to a path at which the file can be retrieved, likely containing the 
  file's given name so that clients suggest a reasonable filename and extension.
* For content that have access restrictions, the simple URL may return a redirect to interactive authentication 
  (university single sign-on, for example)


