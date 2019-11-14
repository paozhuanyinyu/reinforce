package com.reinforce.gradle

class CommandResult {
    def outputStr = ""
    def errorStr = ""

    CommandResult(outputStr, errorStr) {
        this.outputStr = outputStr
        this.errorStr = errorStr
    }
}