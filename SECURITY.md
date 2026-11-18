# Security Policy

## Reporting a vulnerability

This is a training and portfolio project. Please **do not open a public issue**
for security problems.

Report vulnerabilities privately to **tiagofsantospires@gmail.com** with:

- a short description and the potential impact;
- steps to reproduce (endpoint, payload, headers);
- the affected commit or version, if known.

You can expect an acknowledgement within a few days. Please give a reasonable
amount of time to investigate and fix the issue before any public disclosure.

## Scope notes

- The demo accounts, the committed development JWT secret and the default
  webhook secret exist only to make local development convenient. They are not
  valid credentials for a real deployment; `prod` requires `JWT_SECRET_KEY`,
  `ADMIN_EMAIL` and `ADMIN_PASSWORD` to be supplied explicitly.
- The payment gateway is a stub. It deliberately simulates provider responses
  and does not talk to a live payment service provider.
- The project is MIT licensed; see [LICENSE](LICENSE). It is a personal
training project, so pull requests are not expected.
