import os
import subprocess
#Powers of 21024, 2048, 4096, 8192,16384, 32768, 65536, 131072, 262144, 524288, 1048576
######################################################################
#Experiment 1
import os
import subprocess
#Powers of 21024, 2048, 4096, 8192,16384, 32768, 65536, 131072, 262144, 524288, 1048576
######################################################################
#Experiment 1
output_file1="experiment1_2/experiment_stats.csv"
f = open(output_file1, 'w')
l1_sizes=[1024, 2048, 4096, 8192,16384, 32768, 65536, 131072, 262144, 524288, 1048576]
assoc= [1, 2, 4 ,8]
#For everything not direct mapped
for j in assoc:
	for i in l1_sizes:
		java_command="java sim_cache 32 "+str(i)+" "+str(j)+" 0 0 0 ../gcc_trace.txt "
		print java_command
		f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

for i in l1_sizes:
	assoc=(i)/(32)
	java_command="java sim_cache 32 "+str(i)+" "+str(assoc)+" 0 0 0 ../gcc_trace.txt "
	print java_command
	f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

f.close()
####################################################################
#Experiment 3
l1_sizes3=[1024, 2048, 4096, 8192,16384, 32768, 65536, 131072, 262144]
assoc3= [1, 2, 4 ,8]
output_file3="experiment3/experiment_stats.csv"
f = open(output_file3, 'w')
#For everything not direct mapped

for j in assoc3:
	for i in l1_sizes3:
		java_command="java sim_cache 32 "+str(i)+" "+str(j)+" 0 524288 8 ../gcc_trace.txt "
		print java_command
		f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

for i in l1_sizes3:
	assoc=(i)/(32)
	java_command="java sim_cache 32 "+str(i)+" "+str(assoc)+" 0 524288 8 ../gcc_trace.txt "
	print java_command
	f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

f.close()
# ####################################################################
# #Experiment 4
l1_sizes4=[1024, 2048, 4096, 8192,16384, 32768]
block_size4= [16, 32, 64, 128]
output_file4="experiment4/experiment_stats.csv"
f = open(output_file4, 'w')
#For everything not direct mapped
for j in block_size4:
	for i in l1_sizes4:
		java_command="java sim_cache "+str(j)+" "+str(i)+" 4 0 0 0 ../gcc_trace.txt "
		print java_command
		f.write(str(subprocess.check_output(java_command, shell=True))+'\n')
f.close()
# ####################################################################
# #Experiment 5
l1_sizes5=[1024, 2048, 4096, 8192,16384, 32768, 65536, 131072, 262144]
l2_sizes5= [32768, 65536, 131072, 262144, 524288, 1048576]
output_file5="experiment5/experiment_stats.csv"
f = open(output_file5, 'w')
for j in l2_sizes5:
	for i in l1_sizes5:
		java_command="java sim_cache 32 "+str(i)+" 4 0 "+str(j)+" 8 ../gcc_trace.txt "
		print java_command
		f.write(str(subprocess.check_output(java_command, shell=True))+'\n')
f.close()
# ####################################################################
#Experiment 6
l1_sizes6=[1024, 2048, 4096, 8192,16384, 32768]
vc_size= [0, 2, 4, 8, 16]
output_file6="experiment6/experiment_stats.csv"
f = open(output_file6, 'w')
#For everything direct mapped
for j in vc_size:
	for i in l1_sizes6:
		java_command="java sim_cache 32 "+str(i)+" 1 "+str(j)+"  65536 8 ../gcc_trace.txt "
		print java_command
		f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

for i in l1_sizes6:
	java_command="java sim_cache 32 "+str(i)+" 2 0 65536 8 ../gcc_trace.txt "
	print java_command
	f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

for i in l1_sizes6:
	java_command="java sim_cache 32 "+str(i)+" 4 0 65536 8 ../gcc_trace.txt "
	print java_command
	f.write(str(subprocess.check_output(java_command, shell=True))+'\n')

f.close()