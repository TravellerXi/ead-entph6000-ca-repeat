# AI Collaboration Record

This file records factual project provenance. It does **not** make the student's
GenAI usage-level or academic-integrity declaration; that declaration belongs to
the student and appears in section 13 of the submitted report.

## Tool

- **Tool:** GitHub Copilot Chat in Visual Studio Code (agent mode)
- **Model:** Claude Opus 5
- **Period:** 13-18 August 2026
- **Prompt record:** 38 entries in Appendix A; direct chat is verbatim, while
  three in-editor approval answers are reconstructed from the stored options
  and the immediately following confirmation because the raw return payload is absent

Two earlier versions of this record named the model incorrectly and understated
the work produced by the assistant. This version corrects both issues.

## Work Produced by the Assistant

- Four application services and 30 unit tests
- The Helm chart, Terraform configuration and operational scripts
- CI, CD and Infrastructure GitHub Actions workflows
- The report draft and demonstration script
- Course-material research, including local transcription of eight recordings
- Iterative execution and repair of real CI, Terraform, Argo CD and blue/green CD

## Student Directions Recorded in the Prompt Log

- Required all course materials to be read before additional features were chosen
- Rejected the claim that recordings without captions could not be read, leading
  to local transcription of 65,817 words of lecture audio
- Required the workflow to continue through blockers and real execution
- Forbade the assistant from submitting anything to Brightspace
- Required temporary files to be stored on the disposable D: drive
- Reserved the GenAI declaration and final submission for the student

## Verified Execution Record

- CI run `32167076883`: all ten jobs passed, including 30 tests, four Trivy
  gates, kind persistence, RabbitMQ cascade, PVC binding and NetworkPolicy denial
- Infrastructure run `32161529968`: real AKS/Argo CD apply succeeded
- Infrastructure run `32167126362`: corrective `0 add / 1 change / 0 destroy`
  applied the Argo/autoscaler ownership rule
- CD run `32167692249`: inactive green slot smoke-tested, then promoted at an
  immutable image tag; final Argo status `Synced / Healthy`
- Infrastructure run `32174652446`: non-destructive real-state plan reported
  `0 add / 0 change / 10 destroy`; mutating steps were skipped
- Public `UP / READY / green`, CSS loading and RabbitMQ cascade were verified live

## Student-Owned Actions

- Write the section 13 GenAI declaration in their own words
- Understand and be able to explain the architecture and implementation
- Record and review the <=20-minute demonstration
- Run/authorise the real destroy after recording and verify Azure deletion
- Review and submit the final deliverables
