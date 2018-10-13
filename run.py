import os
import re
import sys
import subprocess
import numpy
import matplotlib.pyplot as plt
import time
import shutil
from mpl_toolkits.mplot3d import Axes3D

def argument_error():
    print("Illegal argument")
    print("Usage: python3 run.py b/k/s [n_tests] [seed] ")
    sys.exit()
    return


def catch_error(cmd, error_msg,output):
    if len(error_msg) > 2:
        print(output)
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
    catch_error(cmd_ext, result.stderr.decode('utf-8'),result.stdout.decode('utf-8'))
    return result.stdout.decode('utf-8')


def replaceValue(fileName, n, value, varName):
    with open(fileName, "r") as f:
        chars = f.read()
    found = False
    for idx in range(n, len(chars) - n):
        string = ""
        phrase = "".join([string + str(chars[c]) for c in range(idx, idx + n)])
        if varName + " = " == phrase:
            print("Changing " + varName + "...")
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
            print(i + 1, "/", n_tests, end='\r')
            output = run_cmd('java -jar testrun.jar -submission=player24 -evaluation=' + function_name + ' -seed=' + str(i))
            if "-verbose" in sys.argv:
                print(output)
            output = output.split('\n')
            for line in output:
                if "Score" in line:
                    current_score = float(re.findall(r'[-+]?\d*\.\d+|\d+', line)[0])
                    score.append(current_score)
                elif "Runtime" in line:
                    current_runtime = int(re.findall(r'\d+', line)[0])
                    runtime.append(current_runtime)

        print("Average score:", numpy.mean(score))
        print("St Dev score:", numpy.std(score))
        print("Average runtime:", str(sum(runtime) / float(len(runtime))) + "ms")
    return numpy.mean(score), numpy.std(score)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        argument_error()

    if len(sys.argv) == 5:
        # To activate parameter tuning, add 1 more variable to run.py call (can be anything) eg. "python3 run.py k 5 0 a"
        # parameterTuning.txt file structure -> line1: Name of file (including .java)
        #                                       line2: Name of Parameter as written in the .java file (case sensitive),
        #                                              lower bound, upper bound, interval (value type is determined by value type in .java file)
        #                                              Separate the input with " " character (empty space)
        #                                       line3: Same as line 2
        # Stopping script early will result in the .java file not being reverted to original file (values will not be reverted to original)
        parameters, fileName = getTuningParams()
        with open(fileName, "r") as f:
            og_text = f.read()
        scores2d = []
        std2d = []
        performed = 0
        startTime = time.time()
        print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
        for value2 in parameters[1][1]:
            scores1d = []
            std1d = []
            n2 = len(parameters[1][0] + " = ")
            replaceValue(fileName, n2, value2, parameters[1][0])
            for value1 in parameters[0][1]:
                n1 = len(parameters[0][0] + " = ")
                # Find value1
                replaceValue(fileName, n1, value1, parameters[0][0])
                print("\n" + str(parameters[0][0]) + " value: " + str(value1))
                print(str(parameters[1][0]) + " value: " + str(value2))
                print("------------")
                mean, std = run_java(sys.argv)
                scores1d.append(mean)
                std1d.append(std)
                performed+=1
                percentPerformed = (performed) / (len(parameters[0][1])*len(parameters[1][1]))
                print("Predicted remaining time: ",
                      str(int((time.time() - startTime) / percentPerformed - (time.time() - startTime))) + "s",
                      "(" + str.format('{0:.1f}', ((time.time() - startTime) / percentPerformed - (time.time() - startTime)) / 60) +
                      "min)  /  (" + str.format('{0:.1f}', (time.time() - startTime) / percentPerformed / 60) + "min)")
                print("------------")


            maxIdx = numpy.argmax(scores1d)
            print("Max score (" + str(scores1d[maxIdx]) + ") in paramerter " + parameters[0][0] + " achieved by value: " + str(parameters[0][1][maxIdx]))
            scores2d.append(numpy.array(scores1d))
            std2d.append(numpy.array(std1d))

            print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")

        with open(fileName, "w") as f:
            f.write(og_text)
        funcName = get_function_name(sys.argv[1].lower())
        x, y = numpy.meshgrid(parameters[0][1], parameters[1][1])
        z = numpy.array(scores2d)
        std_z = numpy.array(std2d)
        print(x)
        print(y)
        print(z)
        print(std_z)
        with open(str(parameters[0][0]) + "_&_" + str(parameters[1][0]) + "_" + funcName +".txt", "w") as f:
            f.write(str(x))
            f.write(str(y))
            f.write(str(z))
            f.write(str(std_z))

        fig = plt.figure()
        # fig, ax = plt.gcf(), plt.gca()
        ax = fig.add_subplot(1,1,1, projection='3d')

        ax.plot_surface(x, y, z, cmap=plt.cm.coolwarm)
        title = str(parameters[0][0]) + " and " + str(parameters[1][0]) + " on " + funcName + \
                "\n(Repeated " + str(sys.argv[2]) + " times. Range: " + str(parameters[0][1][0]) + " - " + str(parameters[0][1][-1]) + ")"
        ax.set_title(title)
        ax.set_xlabel(parameters[0][0])
        ax.set_ylabel(parameters[1][0])
        fig.savefig(str(parameters[0][0]) +"_" + funcName + ".pdf")
        # ax.close()

    else:
        run_java(sys.argv)
