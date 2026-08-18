# AI Collaboration Record

This file records factual project provenance. It does **not** make the student's
GenAI usage-level or academic-integrity declaration; that declaration belongs to
the student and appears in section 13 of the submitted report.

## Tool

- **Tool:** GitHub Copilot Chat in Visual Studio Code (agent mode)
- **Model:** Claude Opus 5
- **Period:** 13-18 August 2026
- **Full prompts:** reproduced verbatim in Appendix A of the report

Two earlier versions of this record named the model incorrectly and understated
the work produced by the assistant. This version corrects both issues.

## Work Produced by the Assistant

- Four application services and 29 unit tests
- The Helm chart, Terraform configuration and operational scripts
- CI, CD and Infrastructure GitHub Actions workflows
- The report draft and demonstration script
- Course-material research, including local transcription of eight recordings
- Iterative execution and repair of the real CI pipeline and Terraform plan

## Student Directions Recorded in the Prompt Log

- Required all course materials to be read before additional features were chosen
- Rejected the claim that recordings without captions could not be read, leading
  to local transcription of 65,817 words of lecture audio
- Required the workflow to continue through blockers and real execution
- Forbade the assistant from submitting anything to Brightspace
- Required temporary files to be stored on the disposable D: drive
- Reserved the GenAI declaration and final submission for the student

## Verified Execution Record

- CI run `31992514705`: all ten jobs passed, including Trivy scans, a kind
  deployment, a frontend-to-MongoDB round trip, PVC binding and a negative
  NetworkPolicy test
- Infrastructure run `32131375061`: remote-state init, formatting, validation and
  Terraform plan passed; apply and destroy were skipped
- Infrastructure plan: 10 to add, 0 to change, 0 to destroy

## Student-Owned Actions

- Write the section 13 GenAI declaration in their own words
- Understand and be able to explain the architecture and implementation
- Record the demonstration video
- Approve the cost-incurring Azure deployment and subsequent destruction
- Review and submit the final deliverables
