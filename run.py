import os
import re
import sys
import subprocess

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


def run_cmd(cmd_ext):
    cmd = cmd_ext.split()
    result = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    catch_error(cmd_ext, result.stderr.decode('utf-8'))
    return result.stdout.decode('utf-8')


if __name__ == '__main__':
    if len(sys.argv) < 2:
        argument_error()

    function_name = get_function_name(sys.argv[1].lower())

    # ADD NEW CLASSES TO THE TWO FOLLOWING LINES
    _ = run_cmd('javac -cp contest.jar player24.java Population.java Child.java')
    _ = run_cmd('jar cmf MainClass.txt submission.jar player24.class Population.class Child.class')

    os.putenv("LD_LIBRARY_PATH", os.getcwd())
    output = run_cmd('java -jar testrun.jar -submission=player24 -evaluation=' + function_name + ' -seed=12345')

    score = []
    runtime = []

    if len(sys.argv) < 3:
        print(output)
    else:
        try:
            n_tests = int(sys.argv[2])
        except ValueError:
            argument_error()
        for i in range(n_tests):
            seed = 12345 + i
            print(i + 1, "/", n_tests, end='\r')
            output = run_cmd('java -jar testrun.jar -submission=player24 -evaluation=' + function_name + ' -seed=' + str(seed))
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
