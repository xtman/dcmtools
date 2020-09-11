#!/bin/bash

CWD=$(pwd)
WD=$(cd $(dirname $0); pwd)

# SETTINGS
ARTIFACT_ID=dcmtools
NAME=dcmtools
GROUP_ID=io.github.xtman
POM_FILE=${WD}/mvn-install-jar.xml

# make sure mvn-install-jar.pom.xml exists
[[ ! -f ${POM_FILE} ]] && echo "${POM_FILE} not found" 1>&2 && exit 1

REPO_NAME=mvn-repo
REPO_URL=git@github.com:xtman/${REPO_NAME}.git
REPO_ROOT=${WD}/target/tmp
REPO_DIR=${REPO_ROOT}/${REPO_NAME}


# build jar
cd ${WD}
echo "Building..."
mvn clean package
echo ""

# locate the jar file
cd ${WD}/target

# make sure target/dcmtools-*.jar exists
[[ $(ls dcmtools-*.jar | wc -w) -ne 1 ]] && echo "None or multiple dcmtools-*.jar files found." 1>&2 && exit 2

FILE_NAME=$(ls dcmtools-*.jar)
VERSION=${FILE_NAME#dcmtools-}
VERSION=${VERSION%.jar}
FILE=$(pwd)/${FILE_NAME}

# clone git repo
mkdir -p ${REPO_ROOT}
cd ${REPO_ROOT}
echo "Cloning git repository: ${REPO_URL}"
git clone ${REPO_URL}
echo ""

# check if pre-exists
if [[ -d ${REPO_DIR}/io/github/xtman/dcmtools/${VERSION} ]]; then
    rm -fr ${REPO_DIR}
    echo "${FILE_NAME} already exists on remote repository. Nothing is done. Quit!" 1>&2
    exit 3
fi

# edit git user
echo "[user]" >> ${REPO_DIR}/.git/config
echo "    email=wliu1976@gmail.com" >> ${REPO_DIR}/.git/config
echo "    name = Wilson Liu" >> ${REPO_DIR}/.git/config	

# install jar to repo
echo "installing jar to local maven repository..."
echo ""
cd ${REPO_DIR}
echo "Artifact ID: ${ARTIFACT_ID}"
echo "Version: ${VERSION}"
echo "NAME: ${NAME}"
echo "Group ID:${GROUP_ID}"
echo "POM file: ${POM_FILE}"
echo "File: ${FILE}"
echo "Local repository: ${REPO_DIR}"
mvn install:install-file -DpomFile=${POM_FILE} -Dfile=${FILE} -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} -Dname=${NAME} -Dversion=${VERSION} -Dpackaging=jar -DperformRelease=true -DcreateChecksum=true -DlocalRepositoryPath=${REPO_DIR}
echo""

# push to git
cd ${REPO_DIR}
git add --all
git commit -m "update ${FILE}"
echo "pushing to ${REPO_URL}"
git push
echo ""


# remove local repo
cd ${REPO_ROOT}
echo "removing local repository: ${REPO_DIR}"
rm -fr ${REPO_DIR}
echo ""

cd $CWD