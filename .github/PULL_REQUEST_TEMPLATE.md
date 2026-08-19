<!--
^ Summarise what this changes and why, above this comment. ^

Title this pull request as a Conventional Commit — `feat(scope): …`, `fix: …`.
Release notes are generated from commit titles, so a title that does not parse
lands under "💬 Other" instead of its section.

If it closes an issue, write "Closes #123" so GitHub links the two.
If it changes behaviour a user can see, say what the old behaviour was.
-->

## Things done

<!-- Check what applies. These are not hard requirements — they tell a reviewer
     what you have already covered and where to look. Delete a group that has
     nothing to do with this change rather than leaving it unchecked. -->

- Tested, as applicable:
  - [ ] Ran the jar on a server and walked through the behaviour this changes.
  - [ ] Covered by a new or updated test.
  - [ ] Nothing to test — documentation, formatting or build-only change.
- [ ] Stays on the 1.16 compatibility floor: no Paper API added after 1.16, and
      nothing that only exists on a newer server. The build targets Java 17 and
      compiles either way — the break only shows up on an old server.
- [ ] Config changes ship with a migration: a new key has a default and an
      existing `config.yml` keeps working without hand-editing.
- [ ] New user-facing strings go through the message config, not a literal in
      Java, and every message file gained the key.
- API compatibility, where this touches the `api` module:
  - [ ] Source- and binary-compatible with the published version, or
  - [ ] Breaking, and the next version reflects it.
- [ ] Documentation updated, where this changes something a user can see. The
      docs site lives in [BX-Team/code] — open a pull request there, then paste
      its link here and this one's link there, so a reviewer can see the two
      halves belong together.
- [ ] This pull request has one subject. A formatting sweep bundled with a fix is
      harder to review and harder to revert.
- [ ] Fits [CONTRIBUTING.md].

<!--
Every build of this pull request attaches the jar to its run — the Artifacts box
at the bottom of the run summary. No label needed here.

Found a security issue? Do not open a pull request in the open — see [SECURITY.md].
-->

[CONTRIBUTING.md]: https://github.com/BX-Team/.github/blob/master/CONTRIBUTING.md
[SECURITY.md]: https://github.com/BX-Team/.github/blob/master/SECURITY.md
[BX-Team/code]: https://github.com/BX-Team/code
