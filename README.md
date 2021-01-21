# dialogos-plugin-api
A plugin to allow for API requests.
## How to use:
1. Select if the API request is plain text or should be evaluated as an expression.
The latter allows for adding variables in the url to dynamically adjust it.
2. Select the response type. Currently only JSON responses are supported.
3. Enter the URL/request.
4. Select an existing ``struct`` variable to store the result.
## Example
The ``examples`` directory contains a graph file that retrieves the current price of one Bitcoin in euro and returns it
using Speech Synthesis.