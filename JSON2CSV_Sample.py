#!/usr/bin/python
import sys, os, subprocess,fnmatch, shutil, csv,re, datetime
import time
import json


features = ["wrapsLoop","wrapsTryCatch","wrapsIfElse","wrongMethodRef","constChange","unwrapIfElse","unwrapTryCatch","expArithMod","codeMove","expLogicExpand","condBlockOthersAdd","wrapsElse","wrapsMethod","wrongVarRef","condBlockRem","unwrapMethod","singleLine","missNullCheckP","missNullCheckN","condBlockExcAdd","notClassified","copyPaste","condBlockRetAdd","expLogicReduce","expLogicMod","wrapsIf"]
def travFolder(filepath):
    jsonfiles = os.listdir(filepath)
    for jf in jsonfiles:
        with open (filepath+jf,'r') as currentfile:
            lines = currentfile.readlines()
            values=""
            for line in lines:
                if '"file_name"' in line:
                    s=line.replace('\r','').replace('\n','').replace(',','')
                    jfilename=s.split(":")[1]
                    jfilename=jfilename.replace('"','').replace(' ','')
                    values+=jfilename+','
                    patchedclass = jfilename.split('_')[-1]
                    patchid = jfilename.split('_'+patchedclass)[0]
                    values+='0,'

            for fes in features:
                for line in lines:
                    if fes in line:
                        s=line.replace('\r','').replace('\n','').replace(',','')
                        values+=s.split(":")[1]+','
                        break
        
        with open ("features.csv",'a') as resultfile:
            if len(values.split(',')) > 10:
                resultfile.write(values[:-1]+',\n')

if __name__ == '__main__':

    travFolder('./JSON_Folder/')
