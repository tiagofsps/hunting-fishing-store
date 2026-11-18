# Security Policy

## Reporting a vulnerability

Please **do not open a public issue** for security problems.

Private vulnerability reporting is enabled on this repository. Use the
[Security tab](https://github.com/tiagofsps/hunting-fishing-store/security) →
**Report a vulnerability**. The report stays private between you and the
maintainer, and the fix can be discussed there.

Please include:

- a short description and the potential impact;
- steps to reproduce (endpoint, payload, headers);
- the affected commit or version, if known.

This is a personal training project maintained in spare time, so a reply may
take a few days.

## Scope notes

- The demo accounts, the development JWT secret and the default webhook secret
  exist only to make local development convenient. They are not valid
  credentials for a real deployment; `prod` requires `JWT_SECRET_KEY`,
  `ADMIN_EMAIL` and `ADMIN_PASSWORD` to be supplied explicitly.
- The payment gateway is a stub. It deliberately simulates provider responses
  and does not talk to a live payment service provider.
- The project is MIT licensed; see [LICENSE](LICENSE). It is a personal training
  project, so pull requests are not expected.
