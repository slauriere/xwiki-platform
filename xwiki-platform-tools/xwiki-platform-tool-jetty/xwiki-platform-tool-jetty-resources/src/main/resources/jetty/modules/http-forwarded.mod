# ---------------------------------------------------------------------------
# See the NOTICE file distributed with this work for additional
# information regarding copyright ownership.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.
# ---------------------------------------------------------------------------

# DO NOT EDIT - See: https://jetty.org/docs/index.html

[description]
Enables processing of the "Forwarded" HTTP header (and its predecessors "X-Forwarded-*" HTTP headers).
The "Forwarded" HTTP header is added by intermediaries to provide information about the clients.

[tags]
connector

[depend]
http

[xml]
etc/jetty-http-forwarded.xml

[ini-template]
# tag::documentation[]
### ForwardedRequestCustomizer Configuration

## Whether to process only the RFC7239 "Forwarded" header.
## "X-Forwarded-*" headers are not processed.
# jetty.httpConfig.forwardedOnly=false

## Whether the address obtained from "Forwarded: by=" or
## "X-Forwarded-Server" is used in the request authority.
# jetty.httpConfig.forwardedProxyAsAuthority=false

## Whether the "X-Forwarded-Port" header is used in the request authority,
## or else it is the remote client port.
# jetty.httpConfig.forwardedPortAsAuthority=true

## The name of the RFC 7239 HTTP header.
# jetty.httpConfig.forwardedHeader=Forwarded

## The name of the obsolete forwarded host HTTP header.
# jetty.httpConfig.forwardedHostHeader=X-Forwarded-Host

## The name of the obsolete forwarded server HTTP header.
# jetty.httpConfig.forwardedServerHeader=X-Forwarded-Server

## The name of the obsolete forwarded scheme HTTP header.
# jetty.httpConfig.forwardedProtoHeader=X-Forwarded-Proto

## The name of the obsolete forwarded for HTTP header.
# jetty.httpConfig.forwardedForHeader=X-Forwarded-For

## The name of the obsolete forwarded port HTTP header.
# jetty.httpConfig.forwardedPortHeader=X-Forwarded-Port

## The name of the obsolete forwarded https HTTP header.
# jetty.httpConfig.forwardedHttpsHeader=X-Proxied-Https

## The name of the obsolete forwarded SSL session ID HTTP header.
# jetty.httpConfig.forwardedSslSessionIdHeader=Proxy-ssl-id

## The name of the obsolete forwarded SSL cipher HTTP header.
# jetty.httpConfig.forwardedCipherSuiteHeader=Proxy-auth-cert
# end::documentation[]
