import os
import re
import sys
import subprocess
import numpy
import matplotlib.pyplot as plt
import time
import shutil

def argument_error():
    print("Illegal argument")
    print("Usage: python3 run.py b/k/s [n_tests] [seed] ")
    sys.exit()
    return


def catch_error(cmd, error_msg):
    if len(error_msg) > 2:
        print("Issued command:")
        print(cmd)
        print()
        print("Produced error:")
        print(error_msg)
        sys.exit()
    return


def get_function_name(arg):
    if arg == 'b':
        return 'BentCigarFunction'
    elif arg == 'k':
        return 'KatsuuraEvaluation'
    elif arg == 's':
        return 'SchaffersEvaluation'
    else:
        argument_error()
    return None


def getTuningParams():
    with open("parameterTuning.txt", "r") as f:
        data = f.read().splitlines()
    parameters = []
    rowNum = 0
    fileName =None
    for row in data:
        if rowNum == 0:
            rowNum += 1
            fileName = row
            continue
        items = row.split(" ")
        print("Input: " + str(items))
        if len(items) != 4:
            print("parameterTuning.txt file is written very poorly, ma boi!")
            quit()
        values = []
        a = float(items[1])
        while a <= float(items[2]):
            values.append(a)
            a += float(items[3])
        parameters.append([items[0], values])
    print("Values: " + str(parameters[0]))
    print("Values: " + str(parameters[1]))
    return parameters, fileName


def run_cmd(cmd_ext):
    cmd = cmd_ext.split()
    result = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    catch_error(cmd_ext, result.stderr.decode('utf-8'))
    return result.stdout.decode('utf-8')


def replaceValue(chars, fileName, n, value, varName):
    found = False
    for idx in range(n, len(chars) - n):
        string = ""
        phrase = "".join([string + str(chars[c]) for c in range(idx, idx + n)])
        if varName + " = " == phrase:
            found = True
            decimal = False
            hashlist = list(chars)
            i = n
            while (hashlist[idx + i] != ";"):
                if hashlist[idx + i] == ".":  # Check if there is a decimal in the original
                    decimal = True
                hashlist[idx + i] = ""
                i += 1
            if decimal:
                hashlist.insert(idx + n, str(value))
            else:
                hashlist.insert(idx + n, str(int(value)))
            chars = "".join(hashlist)
            break
    if not found:
        print("Param was not found: ", varName)
        quit()
    with open(fileName, "w") as f:
        f.write(chars)

def run_java(argv):
    function_name = get_function_name(argv[1].lower())

    # ADD NEW CLASSES TO THE TWO FOLLOWING LINES
    _ = run_cmd('javac -cp contest.jar player24.java Population.java Child.java')
    _ = run_cmd('jar cmf MainClass.txt submission.jar player24.class Population.class Population$1.class Child.class')

    os.putenv("LD_LIBRARY_PATH", os.getcwd())
    output = run_cmd('java -jar testrun.jar -submission=player24 -evaluation=' + function_name + ' -seed=12345')

    score = []
    runtime = []

    if len(sys.argv) < 3:
        print(output)
    else:
        try:
            n_tests = int(argv[2])
        except ValueError:
            argument_error()
        for i in range(n_tests):
            seed = 12345 + i
            print(i + 1, "/", n_tests, end='\r')
            output = run_cmd(
                'java -jar testrun.jar -submission=player24 -evaluation=' + function_name + ' -seed=' + str(seed))
            output = output.split('\n')
            for line in output:
                if "Score" in line:
                    current_score = float(re.findall(r'[-+]?\d*\.\d+|\d+', line)[0])
                    score.append(current_score)
                elif "Runtime" in line:
                    current_runtime = int(re.findall(r'\d+', line)[0])
                    runtime.append(current_runtime)
        print("Average score:", sum(score) / len(score))
        print("Average runtime:", str(sum(runtime) / float(len(runtime))) + "ms")
    return sum(score) / len(score)


if __name__ == '__main__':
    if len(sys.argv) < 2:
        argument_error()

    if len(sys.argv) == 5:
        # To activate parameter tunning, add 1 more variable to run.py call (can be anything) eg. "python3 run.py k 5 0 a"
        # parameterTuning.txt file structure -> line1: Name of file (including .java)
        #                                       line2: Name of Parameter as written in the .java file (case sensitive),
        #                                              lower bound, upper bound, interval (value type is determined by value type in .java file)
        #                                              Separate the input with " " character (empty space)
        #                                       line3: Same as line 2
        # Stopping script early will result in the .java file not being reverted to original file (values will not be reverted to original)
        parameters, fileName = getTuningParams()
        with open(fileName, "r") as f:
            og_text = f.read()
        scores = []
        print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
        for value2 in parameters[1][1]:
            for value1 in parameters[0][1]:
                with open(fileName, "r") as f:
                    chars = f.read()
                    n1 = len(parameters[0][0] + " = ")
                    # Find value1
                    replaceValue(chars, fileName, n1, value1, parameters[0][0])
                    n2 = len(parameters[1][0] + " = ")
                    replaceValue(chars, fileName, n2, value2, parameters[1][0])

                with open(fileName, "w") as f:
                    f.write(chars)
                print("\n" + str(parameters[0][0]) + " value: " + str(value1))
                print(str(parameters[1][0]) + " value: " + str(value2))
                print("------------")
                startTime = time.time()
                scores.append([run_java(sys.argv)])
                print("Predicted total duration: ",
                      str((time.time() - startTime) * len(parameters[0][1])*len(parameters[1][1])) + "s",
                      "(" + str((time.time() - startTime) * len(parameters[0][1]*len(parameters[1][1])) // 60) + "min)")
                print("------------")
            print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")

        with open(fileName, "w") as f:
            f.write(og_text)
        maxIdx = numpy.argmax(scores)
        print(scores)
        print(maxIdx)
        print("Max score (" + str(scores[maxIdx]) + ") in paramerter " + parameters[0][0] + " achieved by value: " + str(parameters[0][1][maxIdx]))
        funcName = function_name = get_function_name(sys.argv[1].lower())
        fig, ax = plt.gcf(), plt.gca()
        plt.plot(parameters[0][1], scores)
        plt.title(parameters[0][0] + " on " + funcName + "\n(Repeated " + str(sys.argv[2]) + "times. Range: " + str(parameters[0][1][0]) + " - " + str(parameters[0][1][-1]) + ")")
        fig.savefig(parameters[0][0] +"_" + funcName + ".pdf")
        plt.close()

    else:
        run_java(sys.argv)