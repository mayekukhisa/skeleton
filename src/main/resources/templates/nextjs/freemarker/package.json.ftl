<#--
 ~ Copyright (c) 2023 Mayeku Khisa
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 -->
{
   "name": "${projectName?lower_case?replace(' |_', '-', 'r')}",
   "version": "0.1.0",
   "private": true,
   "scripts": {
      "dev": "next dev",
      "build": "next build",
      "start": "next start",
      "lint": "next lint && prettier --check ./**/*.{css,js,json,md} --ignore-path .gitignore",
      "lint:fix": "next lint --fix && prettier --write ./**/*.{css,js,json,md} --ignore-path .gitignore"
   },
   "dependencies": {
      "@next/font": "13.1.2",
      "next": "13.1.2",
      "react": "18.2.0",
      "react-dom": "18.2.0"
   },
   "devDependencies": {
      "@types/node": "18.11.18",
      "@types/react": "18.0.27",
      "@types/react-dom": "18.0.10",
      "eslint": "8.32.0",
      "eslint-config-next": "13.1.2",
      "eslint-config-prettier": "8.6.0",
      "eslint-plugin-prettier": "4.2.1",
      "eslint-plugin-simple-import-sort": "9.0.0",
      "typescript": "4.9.4"
   }
}
