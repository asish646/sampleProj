#
# Copyright (c) Microsoft Corporation.
# All rights reserved.
#
# This code is licensed under the MIT License.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files(the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions :
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

# This file is designed to use the python http logic to attach a client certificate to a request without needing to use any store on the machine

import getopt
import sys
import json
import ssl
import urllib
import re

try:
  from urllib.parse import urlencode
except ImportError: # backward compatible for python2
  from urllib import urlencode
try:
  from urllib.request import urlopen, Request
except ImportError: # backward compatible for python2
  from urllib2 import urlopen, Request
try:
  from urllib.request import URLError
except ImportError: # backward compatible for python2
  from urllib2 import URLError

def printUsage():
  print('cbauth.py -p <pathToCert> -a <authority> -u <certAuthUrl> -c <authContext> -f <flowToken>')

def main(argv):
  try:
    options, args = getopt.getopt(argv, 'hp:a:u:c:f:')
  except getopt.GetoptError:
    printUsage()
    sys.exit(-1)

  pathToCert = ''
  authority = ''
  certAuthUrl = ''
  authContext = ''
  flowToken = ''

  for option, arg in options:
    if option == '-h':
      printUsage()
      sys.exit()
    elif option == '-p':
      pathToCert = arg
    elif option == '-a':
      authority = arg
    elif option == '-u':
      certAuthUrl = arg
    elif option == '-c':
      authContext = arg
    elif option == '-f':
      flowToken = arg

  if pathToCert == '' or authority == '' or certAuthUrl == '' or authContext == '' or flowToken == '':
    printUsage()
    sys.exit(-1)

  # Build REST call
  headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Accept': 'application/json',
    'Connection': 'keep-alive',
    'Referer': authority,
    'CacheControl': 'no-cache, no-store'
  }

  params = {
    'ctx': authContext,
    'flowToken': flowToken,
  }

  req = Request(
    url = certAuthUrl,
    headers = headers,
    data = urlencode(params).encode("utf-8"))

  try:
    ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
    # may need to download correct CA for this step
    ctx.load_default_certs(purpose=ssl.Purpose.CLIENT_AUTH)
    ctx.load_cert_chain(certfile=pathToCert)
    # allow TLS 1.2 and later
    f = urlopen(req, context=ctx)

    response = f.read()
    f.close()
    sys.stdout.write(response.decode('utf-8'))
  except URLError as err:
    sys.stderr.write("Attempting to acquire token via direct HTTP request to {0}\n".format(authority))
    sys.stderr.write("\tClient-ID: {0}\n".format(clientId))
    sys.stderr.write("\tResource:  {0}\n".format(resource))
    sys.stderr.write("\t=== FAILURE: {0} ===\n".format(err.reason))

if __name__ == '__main__':
  main(sys.argv[1:])
