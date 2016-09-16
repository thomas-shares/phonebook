#!/bin/bash

url="http://localhost:3000/v1/phonebook"

echo "Add entry to phone book..."

entry=`curl -s $url -H "Content-Type: application/data" -X POST -d '{:firstname "fred" :surname "Smith" :phonenumber "012345"}'`

echo $entry

curl -s -X GET ${url} | grep "fred"
if [ $? -eq 1 ] ; then
  echo "didn't find entry... exiting!!!"
  exit 1
fi

curl -s -X PUT ${url}/${entry} -H \"Content-Type: application/data\" -d '{:firstname \"Fred\" :surname \"Smith\" :phonenumber \"012345\"}' 

curl -s -X GET ${url} | grep "Fred"
if [ $? -eq 1 ] ; then
  echo "change didn't work"
  exit 1
fi



